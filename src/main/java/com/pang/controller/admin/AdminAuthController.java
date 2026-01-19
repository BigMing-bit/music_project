package com.pang.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.pang.common.Result;
import com.pang.common.utils.IpUtil;
import com.pang.entity.SysAdmin;
import com.pang.entity.SysOperationLog;
import com.pang.security.dto.AdminLoginDTO;
import com.pang.service.AdminAuthService;
import com.pang.service.AdminService;
import com.pang.service.SysOperationLogService;
import com.pang.utils.SaTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminAuthController {

    @Autowired
    private AdminAuthService adminAuthService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private SysOperationLogService operationLogService;

    @PostMapping("/login")
    public Result login(@RequestBody @Valid AdminLoginDTO dto, HttpServletRequest request) {

        SysAdmin admin = adminAuthService.login(dto.getUsername(), dto.getPassword());
        if (admin == null) {
            return Result.error(401, "用户名或密码错误");
        }

        // ✅ 登录
        SaTokenUtil.ADMIN.login(admin.getId());
        SaTokenUtil.ADMIN.getSession().set("displayName",
                (admin.getDisplayName() != null && !admin.getDisplayName().isBlank())
                        ? admin.getDisplayName()
                        : (admin.getNickname() != null && !admin.getNickname().isBlank() ? admin.getNickname() : admin.getUsername())
        );

        // ✅ 更新最后登录时间
        SysAdmin update = new SysAdmin();
        update.setId(admin.getId());
        update.setLastLoginTime(LocalDateTime.now());
        adminService.updateById(update);

        SysOperationLog log = new SysOperationLog();
        log.setAdminId(admin.getId());
        log.setAdminUsername(admin.getUsername());
        log.setModule("系统管理");
        log.setAction("管理员登录");
        log.setMethod("POST");
        log.setPath("/admin/login");
        log.setParams(null);                 // 登录一般不记参数
        log.setIp(IpUtil.getClientIp(request));
        log.setSuccess(1);
        log.setCreateTime(LocalDateTime.now());

        operationLogService.save(log);
        // 👈 这里交给 Service

        // ✅ 角色 & 权限
        List<String> roleList = adminAuthService.getRoleCodes(admin.getId());
        List<String> permList = adminAuthService.getPermissionCodes(admin.getId());
        SaTokenUtil.ADMIN.getSession().set("roles", roleList);
        SaTokenUtil.ADMIN.getSession().set("permissions", permList);

        Map<String, Object> data = new HashMap<>();
        data.put("token", SaTokenUtil.ADMIN.getTokenValue());
        data.put("tokenName", SaTokenUtil.ADMIN.getTokenName());
        data.put("id", admin.getId());
        data.put("username", admin.getUsername());
        data.put("nickname", admin.getNickname());
        data.put("avatarUrl", admin.getAvatarUrl());
        data.put("displayName", admin.getDisplayName());
        data.put("permissions", permList);
        data.put("roles", roleList);
        return Result.success(data);
    }

    @PostMapping("/logout")
    public Result logout() {
        SaTokenUtil.ADMIN.logout();
        return Result.success("退出成功");
    }

    // 权限测试接口，只有拥有 sys:admin:view 权限的管理员可以访问
    @SaCheckPermission(value = "sys:admin:view", type = "admin")
    @GetMapping("/test-permission")
    public Result testPermission() {
        return Result.success("你有 sys:admin:view 权限，可以访问");
    }
}


