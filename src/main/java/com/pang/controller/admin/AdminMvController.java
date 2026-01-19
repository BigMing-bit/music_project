package com.pang.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.pang.common.Result;
import com.pang.entity.MusicMv;
import com.pang.service.MusicMvService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/mvs")
@Api(tags = "MV管理")
public class AdminMvController {

    @Autowired
    private MusicMvService mvService;

    @SaCheckPermission(value = "music:mv:view", type = "admin")
    @GetMapping
    @ApiOperation("MV分页列表")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long singerId,
                       @RequestParam(required = false) Integer status) {
        return Result.success(mvService.adminPage(pageNum, pageSize, keyword, singerId, status));
    }

    @SaCheckPermission(value = "music:mv:view", type = "admin")
    @GetMapping("/{id}")
    @ApiOperation("MV详情")
    public Result info(@PathVariable Long id) {
        return Result.success(mvService.adminDetail(id));
    }

    @SaCheckPermission(value = "music:mv:add", type = "admin")
    @PostMapping
    @ApiOperation("新增MV")
    public Result add(@RequestBody MusicMv mv) {
        mvService.save(mv);
        return Result.success("保存成功");
    }

    @SaCheckPermission(value = "music:mv:edit", type = "admin")
    @PutMapping
    @ApiOperation("更新MV")
    public Result update(@RequestBody MusicMv mv) {
        mvService.updateById(mv);
        return Result.success("更新成功");
    }

    @SaCheckPermission(value = "music:mv:edit", type = "admin")
    @PutMapping("/{id}/status")
    @ApiOperation("更新MV状态")
    public Result updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        return mvService.updateStatus(id, status) ? Result.success("状态更新成功") : Result.error(400, "更新失败");
    }

    @SaCheckPermission(value = "music:mv:delete", type = "admin")
    @DeleteMapping
    @ApiOperation("批量删除MV")
    public Result delete(@RequestBody Long[] ids) {
        mvService.removeBatchByIdsSafe(List.of(ids));
        return Result.success("删除成功");
    }
}
