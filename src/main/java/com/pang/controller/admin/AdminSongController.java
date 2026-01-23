package com.pang.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pang.common.Result;
import com.pang.common.base.BaseController;
import com.pang.common.log.OpLog;
import com.pang.entity.Song;
import com.pang.entity.vo.SongVo;
import com.pang.security.dto.SongQueryDTO;
import com.pang.service.SongService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/songs")
@Api(tags = "歌曲管理")
@RequiredArgsConstructor
public class AdminSongController extends BaseController {

    private final SongService songService;


    @OpLog(module = "歌曲管理", action = "新增歌曲")
    @SaCheckPermission(value = "music:song:add", type = "admin")
    @PostMapping("save")
    @ApiOperation("新增歌曲")
    public Result save(@RequestBody Song song) {
        if (song.getId() == null) {
            song.setCreateTime(LocalDateTime.now());
        }
        songService.saveOrUpdateWithAlbumName(song);
        return Result.success("新添成功");
    }

    @SaCheckPermission(value = "music:song:edit", type = "admin")
    @PutMapping
    @ApiOperation("更新歌曲")
    public Result updateSong(@RequestBody Song song) {
        songService.saveOrUpdateWithAlbumName(song);
        return Result.success("更新成功");
    }

    @SaCheckPermission(value = "music:song:edit", type = "admin")
    @PutMapping("/{id}/status")
    @ApiOperation("更新歌曲状态")
    public Result updateSongStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        songService.lambdaUpdate()
                .eq(Song::getId, id)
                .set(Song::getStatus, params.get("status"))
                .update();
        return Result.success("更新成功");
    }

    @OpLog(module = "歌曲管理", action = "删除歌曲")
    @SaCheckPermission(value = "music:song:delete", type = "admin")
    @DeleteMapping
    @ApiOperation("批量删除歌曲")
    public Result delete(@RequestBody List<Long> ids) {
        songService.removeBatchByIds(ids);
        return Result.success("删除成功");
    }

    @GetMapping("/list")
    @ApiOperation("获取歌曲列表（分页 + join）")
    public Result getSongList(SongQueryDTO queryDTO) {
        IPage<SongVo> page = songService.pageSongVo(queryDTO);
        return Result.success(page);
    }


    @SaCheckPermission(value = "music:song:view", type = "admin")
    @GetMapping("/list/{id}")
    @ApiOperation("获取歌曲详情")
    public Result info(@PathVariable Long id) {
        Song song = songService.getById(id);
        return Result.success(song);
    }


    @SaCheckPermission(value = "music:song:view", type = "admin")
    @GetMapping("/select")
    public Result selectSongs(@RequestParam(required = false) String keyword) {
        return Result.success(songService.selectSongOptions(keyword));
    }


    @SaCheckPermission(value = "music:song:view", type = "admin")
    @GetMapping("/byIds")
    public Result byIds(@RequestParam List<Long> ids) {
        return Result.success(songService.selectSongOptionsByIds(ids));
    }


}