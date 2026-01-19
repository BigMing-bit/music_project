package com.pang.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pang.common.Result;
import com.pang.common.base.BaseController;
import com.pang.common.log.OpLog;
import com.pang.entity.Singer;
import com.pang.service.SingerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/singers")
@Api(tags = "歌手管理")
@RequiredArgsConstructor
public class AdminSingerController extends BaseController {

    private final SingerService singerService;

    @SaCheckPermission(value = "music:singer:view", type = "admin")
    @GetMapping("/list")
    @ApiOperation("获取歌手列表（分页 + 筛选）")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(required = false) Integer gender) {

        IPage<Singer> page = singerService.adminPage(pageNum, pageSize, keyword, status, gender);
        return Result.success(page);
    }

    @SaCheckPermission(value = "music:singer:view", type = "admin")
    @GetMapping("/{id}")
    @ApiOperation("歌手详情")
    public Result info(@PathVariable Long id) {
        return Result.success(singerService.getById(id));
    }


    @OpLog(module = "歌手管理", action = "新增歌手")
    @SaCheckPermission(value = "music:singer:add", type = "admin")
    @PostMapping
    @ApiOperation("新增歌手")
    public Result save(@RequestBody Singer singer) {
        singer.setId(null);
        singer.setCreateTime(LocalDateTime.now());
        singerService.save(singer);
        return Result.success("保存成功");
    }

    @OpLog(module = "歌手管理", action = "更新歌手")
    @SaCheckPermission(value = "music:singer:edit", type = "admin")
    @PutMapping
    @ApiOperation("更新歌手")
    public Result update(@RequestBody Singer singer) {
        Assert.notNull(singer.getId(), "id不能为空");
        singer.setUpdateTime(LocalDateTime.now());
        singerService.updateById(singer);
        return Result.success("更新成功");
    }


    @SaCheckPermission(value = "music:singer:edit", type = "admin")
    @PutMapping("/{id}/status")
    @ApiOperation("更新歌手状态")
    public Result updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Singer singer = new Singer();
        singer.setId(id);
        singer.setStatus(params.get("status"));
        singer.setUpdateTime(LocalDateTime.now());
        singerService.updateById(singer);
        return Result.success("状态更新成功");
    }

    @OpLog(module = "歌手管理", action = "删除歌手")
    @SaCheckPermission(value = "music:singer:delete", type = "admin")
    @DeleteMapping
    @ApiOperation("批量删除歌手")
    public Result delete(@RequestBody Long[] ids) {
        Assert.isTrue(ArrayUtil.isNotEmpty(ids), "请选择删除项");
        singerService.removeBatchByIds(List.of(ids));
        return Result.success("删除成功");
    }

    @SaCheckPermission(value = "music:singer:view", type = "admin")
    @GetMapping("/select")
    @ApiOperation("下拉选择歌手（支持搜索）")
    public Result select(@RequestParam(required = false) String keyword) {
        return Result.success(singerService.adminSelectOptions(keyword));
    }
}
