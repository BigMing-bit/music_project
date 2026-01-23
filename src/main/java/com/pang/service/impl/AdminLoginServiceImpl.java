package com.pang.service.impl;

import com.pang.common.constants.CommonConstants;
import com.pang.common.Result;
import com.pang.common.utils.IpUtil;
import com.pang.entity.SysAdmin;
import com.pang.entity.SysOperationLog;
import com.pang.security.dto.AdminLoginDTO;
import com.pang.service.AdminAuthService;
import com.pang.service.AdminLoginService;
import com.pang.service.AdminService;
import com.pang.service.SysOperationLogService;
import com.pang.utils.SaTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminLoginServiceImpl implements AdminLoginService {

    private final AdminAuthService adminAuthService;
    private final AdminService adminService;
    private final SysOperationLogService operationLogService;



    @Override
    public Result login(AdminLoginDTO dto, HttpServletRequest request) {
        try {
            SysAdmin admin = adminAuthService.login(dto.getUsername(), dto.getPassword());
            if (admin.getStatus() == 0) {
                return Result.error(CommonConstants.ACCOUNT_DISABLED, "账户已禁用，请联系管理员");
            }
            updateLastLoginTime(admin);
            recordLoginLog(admin, request);
            List<String> roleList = adminAuthService.getRoleCodes(admin.getId());
            List<String> permList = adminAuthService.getPermissionCodes(admin.getId());
            storeRolesAndPermissions(roleList, permList);
            Map<String, Object> data = buildLoginResponseData(admin);
            data.put("permissions", permList);
            data.put("roles", roleList);

            return Result.success(data);
        } catch (Exception e) {
            return Result.error(CommonConstants.LOGIN_FAILED, e.getMessage());
        }
    }

    @Override
    public Map<String, Object> buildLoginResponseData(SysAdmin admin) {
        Map<String, Object> data = new HashMap<>();
        data.put("token", SaTokenUtil.ADMIN.getTokenValue());
        data.put("tokenName", SaTokenUtil.ADMIN.getTokenName());
        data.put("id", admin.getId());
        data.put("username", admin.getUsername());
        data.put("nickname", admin.getNickname());
        data.put("avatarUrl", admin.getAvatarUrl());
        data.put("displayName", getDisplayName(admin));
        return data;
    }

    private void updateLastLoginTime(SysAdmin admin) {
        SysAdmin update = new SysAdmin();
        update.setId(admin.getId());
        update.setLastLoginTime(LocalDateTime.now());
        adminService.updateById(update);
    }
    private void recordLoginLog(SysAdmin admin, HttpServletRequest request) {
        SysOperationLog log = new SysOperationLog();
        log.setAdminId(admin.getId());
        log.setAdminUsername(admin.getUsername());
        log.setModule("系统管理");
        log.setAction("管理员登录");
        log.setMethod("POST");
        log.setPath("/admin/login");
        log.setParams(null); // 登录一般不记参数
        log.setIp(IpUtil.getClientIp(request));
        log.setSuccess(1);
        log.setCreateTime(LocalDateTime.now());
        operationLogService.save(log);
    }
    private void storeRolesAndPermissions(List<String> roleList, List<String> permList) {
        SaTokenUtil.ADMIN.getSession().set("roles", roleList);
        SaTokenUtil.ADMIN.getSession().set("permissions", permList);
    }
    private String getDisplayName(SysAdmin admin) {
        if (admin.getDisplayName() != null && !admin.getDisplayName().isBlank()) {
            return admin.getDisplayName();
        } else if (admin.getNickname() != null && !admin.getNickname().isBlank()) {
            return admin.getNickname();
        } else {
            return admin.getUsername();
        }
    }

    @Override
    public Result logout() {
        SaTokenUtil.ADMIN.logout();
        return Result.success("退出成功");
    }
}
