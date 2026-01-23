package com.pang.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.pang.common.Result;
import com.pang.common.log.OpLog;
import com.pang.entity.SysOperationLog;
import com.pang.security.dto.LogQueryDTO;
import com.pang.service.SysOperationLogService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin/logs")
@Api(tags = "日志管理")
@RequiredArgsConstructor
public class AdminLogController {

    private final SysOperationLogService logService;

    @SaCheckPermission(value="system:log:view", type="admin")
    @GetMapping
    public Result page(LogQueryDTO queryDTO) {
        return Result.success(logService.adminPage(queryDTO));
    }

    @SaCheckPermission(value="system:log:view", type="admin")
    @GetMapping("/{id}")
    public Result info(@PathVariable Long id) {
        return Result.success(logService.getById(id));
    }

    @OpLog(module = "日志管理", action = "删除日志")
    @SaCheckPermission(value="system:log:delete", type="admin")
    @DeleteMapping
    public Result delete(@RequestBody List<Long> ids) {
        logService.removeBatchByIds(ids);
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

