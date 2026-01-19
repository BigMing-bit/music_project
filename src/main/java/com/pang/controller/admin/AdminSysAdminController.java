package com.pang.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.pang.common.Result;
import com.pang.entity.SysAdmin;
import com.pang.service.AdminService;
import com.pang.utils.MD5Utils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/admin/sys-admin")
public class AdminSysAdminController {

    private final AdminService adminService;

    public AdminSysAdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ✅ 超级管理员：重置某个管理员密码
    @SaCheckPermission(value = "system:admin:resetPwd", type = "admin")
    @PutMapping("/{id}/reset-password")
    public Result resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.error(400, "新密码不能为空");
        }

        SysAdmin update = new SysAdmin();
        update.setId(id);
        update.setPassword(MD5Utils.encode(newPassword.trim()));
        update.setUpdateTime(LocalDateTime.now());

        boolean ok = adminService.updateById(update);
        return ok ? Result.success("重置成功") : Result.error(500, "重置失败");
    }
}
