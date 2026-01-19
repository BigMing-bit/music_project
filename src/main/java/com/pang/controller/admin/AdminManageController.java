package com.pang.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import com.pang.common.Result;
import com.pang.entity.SysAdmin;
import com.pang.service.AdminService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/admins")
@Api(tags = "管理员管理")
public class AdminManageController {

    @Autowired
    private AdminService adminService;

    @SaCheckPermission(value = "system:admin:view", type = "admin")
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer status) {
        return Result.success(adminService.adminPage(pageNum, pageSize, keyword, status));
    }

    @SaCheckPermission(value = "system:admin:view", type = "admin")
    @GetMapping("/{id}")
    public Result info(@PathVariable Long id) {
        return Result.success(adminService.getById(id));
    }

    @SaCheckPermission(value = "system:admin:add", type = "admin")
    @PostMapping
    public Result save(@RequestBody SysAdmin admin) {
        if (admin.getId() == null) admin.setCreateTime(LocalDateTime.now());
        adminService.save(admin);
        return Result.success("保存成功");
    }

    @SaCheckPermission(value = "system:admin:edit", type = "admin")
    @PutMapping
    public Result update(@RequestBody SysAdmin admin) {
        admin.setUpdateTime(LocalDateTime.now());
        adminService.updateById(admin);
        return Result.success("更新成功");
    }

    @SaCheckPermission(value = "system:admin:edit", type = "admin")
    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        SysAdmin a = new SysAdmin();
        a.setId(id);
        a.setStatus(params.get("status"));
        a.setUpdateTime(LocalDateTime.now());
        adminService.updateById(a);
        return Result.success("状态更新成功");
    }

    @SaCheckPermission(value = "system:admin:delete", type = "admin")
    @DeleteMapping
    public Result delete(@RequestBody Long[] ids) {
        Assert.isTrue(ArrayUtil.isNotEmpty(ids), "请选择删除项");
        adminService.removeBatchByIds(List.of(ids));
        return Result.success("删除成功");
    }
}
