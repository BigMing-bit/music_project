package com.pang.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.pang.common.Result;
import com.pang.security.dto.AdminLoginDTO;
import com.pang.service.AdminLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminLoginService adminLoginService;

    @PostMapping("/login")
    public Result login(@RequestBody @Valid AdminLoginDTO dto, HttpServletRequest request) {
        return adminLoginService.login(dto, request);
    }

    @PostMapping("/logout")
    public Result logout() {
        return adminLoginService.logout();
    }

    // 权限测试接口，只有拥有 sys:admin:view 权限的管理员可以访问
    @SaCheckPermission(value = "sys:admin:view", type = "admin")
    @GetMapping("/test-permission")
    public Result testPermission() {
        return Result.success("你有 sys:admin:view 权限，可以访问");
    }
}
