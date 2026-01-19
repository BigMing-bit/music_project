package com.pang.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pang.common.Result;
import com.pang.common.base.BaseController;
import com.pang.entity.Album;
import com.pang.entity.Query.AlbumQueryParam;
import com.pang.entity.vo.AlbumVo;
import com.pang.service.AlbumService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Api(tags = "专辑管理")
@RestController
@RequestMapping("/admin/albums")
@RequiredArgsConstructor
public class AdminAlbumController extends BaseController {

    private final AlbumService albumService;

    @ApiOperation("获取专辑列表（分页 + join singerName）")
    @SaCheckPermission(value = "music:album:view", type = "admin")
    @GetMapping
    public Result page(AlbumQueryParam albumQueryParam) {

        IPage<AlbumVo> page = albumService.adminPageVo(albumQueryParam);
        return Result.success(page);
    }

    @SaCheckPermission(value = "music:album:view", type = "admin")
    @GetMapping("/{id}")
    @ApiOperation("专辑详情")
    public Result info(@PathVariable Long id) {
        return Result.success(albumService.getById(id));
    }


    @SaCheckPermission(value = "music:album:add", type = "admin")
    @PostMapping
    @ApiOperation("新增专辑")
    public Result save(@Validated @RequestBody Album album) {
        if (album.getId() == null)
            album.setCreateTime(LocalDateTime.now());
        albumService.saveOrUpdate(album);
        return Result.success("保存成功");
    }

    @SaCheckPermission(value = "music:album:edit", type = "admin")
    @PutMapping
    @ApiOperation("更新专辑")
    public Result update(@Validated @RequestBody Album album) {
        album.setUpdateTime(LocalDateTime.now());
        albumService.updateById(album);
        return Result.success("更新成功");
    }

    @SaCheckPermission(value = "music:singer:edit", type = "admin")
    @PutMapping("/{id}/status")
    @ApiOperation("更新歌手状态")
    public Result updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Album album = new Album();
        album.setId(id);
        album.setStatus(params.get("status"));
        album.setUpdateTime(LocalDateTime.now());
        albumService.updateById(album);
        return Result.success("状态更新成功");
    }


    @SaCheckPermission(value = "music:album:delete", type = "admin")
    @DeleteMapping
    @ApiOperation("批量删除专辑")
    public Result delete(@RequestBody Long[] ids) {
        albumService.removeBatchByIds(List.of(ids));
        return Result.success("删除成功");
    }

    @SaCheckPermission(value = "music:album:view", type = "admin")
    @GetMapping("/select")
    @ApiOperation("下拉选择专辑（支持搜索）")
    public Result select(@RequestParam(required = false) String keyword) {
        return Result.success(albumService.adminSelectOptions(keyword));
    }
}
