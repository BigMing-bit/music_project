package com.pang.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pang.common.Result;
import com.pang.common.log.OpLog;
import com.pang.entity.SysOperationLog;
import com.pang.service.SysOperationLogService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin/logs")
@Api(tags = "日志管理")
public class AdminLogController {

    @Autowired
    private SysOperationLogService logService;

    @SaCheckPermission(value="system:log:view", type="admin")
    @GetMapping
    public Result page(@RequestParam Integer pageNum,
                       @RequestParam Integer pageSize,
                       @RequestParam(required=false) String keyword,
                       @RequestParam(required=false) Integer success) {
        Page<SysOperationLog> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<SysOperationLog> qw = new LambdaQueryWrapper<>();
        qw.and(keyword != null && !keyword.isBlank(), w -> w
                .like(SysOperationLog::getAdminUsername, keyword)
                .or().like(SysOperationLog::getModule, keyword)
                .or().like(SysOperationLog::getAction, keyword)
                .or().like(SysOperationLog::getPath, keyword)
                .or().like(SysOperationLog::getIp, keyword)
        );
        qw.eq(success != null, SysOperationLog::getSuccess, success);
        qw.orderByDesc(SysOperationLog::getId);

        return Result.success(logService.page(page, qw));
    }

    @SaCheckPermission(value="system:log:view", type="admin")
    @GetMapping("/{id}")
    public Result info(@PathVariable Long id) {
        return Result.success(logService.getById(id));
    }

    @OpLog(module = "日志管理", action = "删除日志")
    @SaCheckPermission(value="system:log:delete", type="admin")
    @DeleteMapping
    public Result delete(@RequestBody Long[] ids) {
        logService.removeBatchByIds(java.util.List.of(ids));
        return Result.success("删除成功");
    }


    @OpLog(module = "日志管理", action = "清空日志")
    @SaCheckPermission(value="system:log:delete", type="admin")
    @DeleteMapping("/clear")
    public Result clear() {
        logService.remove(null);
        return Result.success("清空成功");
    }
}

