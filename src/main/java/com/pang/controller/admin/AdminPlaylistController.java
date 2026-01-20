package com.pang.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pang.common.Result;
import com.pang.entity.Playlist;
import com.pang.entity.vo.PlaylistDetailVo;
import com.pang.entity.vo.PlaylistVo;
import com.pang.mapper.PlaylistSongMapper;
import com.pang.security.dto.PlaylistSaveDTO;
import com.pang.service.PlaylistService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/playlists")
@Api(tags = "歌单管理")
@RequiredArgsConstructor
public class AdminPlaylistController {

    private final PlaylistService playlistService;

    private final PlaylistSongMapper playlistSongMapper;

    @SaCheckPermission(value = "music:playlist:edit", type = "admin")
    @PostMapping("/save")
    @ApiOperation("新增/编辑歌单（包含歌曲关系）")
    public Result saveOrUpdate(@RequestBody @Validated PlaylistSaveDTO dto) {
        Long id = playlistService.saveOrUpdateWithSongs(dto);
        return Result.success(id);
    }

    @SaCheckPermission(value = "music:playlist:view", type = "admin")
    @GetMapping("/list")
    @ApiOperation("歌单分页")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer status) {

        IPage<PlaylistVo> page = playlistService.pageVo(pageNum, pageSize, keyword, status);
        return Result.success(page);
    }

    @SaCheckPermission(value = "music:playlist:view", type = "admin")
    @GetMapping("/{id}/detail")
    @ApiOperation("歌单详情（含歌曲ID列表）")
    public Result detail(@PathVariable Long id) {
        Playlist p = playlistService.getById(id);
        if (p == null) {
            return Result.error(404, "歌单不存在");
        }

        List<Long> songIds = playlistSongMapper.selectSongIds(id);

        PlaylistDetailVo vo = new PlaylistDetailVo();
        vo.setId(p.getId());
        vo.setName(p.getName());
        vo.setDescription(p.getDescription());
        vo.setCoverUrl(p.getCoverUrl());
        vo.setStatus(p.getStatus());
        vo.setCreatorUserId(p.getCreatorUserId());

        return Result.success(vo);
    }

    @SaCheckPermission(value = "music:playlist:edit", type = "admin")
    @PutMapping("/{id}/status")
    @ApiOperation("更新歌单状态")
    public Result updateStatus(@PathVariable Long id,
                               @RequestBody Map<String, Integer> params) {

        Integer status = params.get("status");
        if (status == null) {
            return Result.error(400, "缺少参数：status");
        }
        Playlist playlist = new Playlist();
        playlist.setId(id);
        playlist.setStatus(status);
        playlistService.updateById(playlist);

        return Result.success("状态更新成功");
    }

    @SaCheckPermission(value = "music:playlist:delete", type = "admin")
    @DeleteMapping
    public Result delete(@RequestBody Long[] ids) {
        List<Long> idList = List.of(ids);
        playlistSongMapper.deleteByPlaylistIds(idList);
        playlistService.removeBatchByIds(idList);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}/songs")
    @SaCheckPermission(value = "music:playlist:view", type = "admin")
    public Result songs(@PathVariable Long id) {
        return Result.success(playlistService.listSongsByPlaylistId(id));
    }


}
