package com.pang.service;

import com.pang.entity.SysAdmin;

import java.util.List;

public interface AdminAuthService {
    SysAdmin login(String username, String password);
    List<String> getPermissionsByAdminId(Long adminId);

    List<String> getRolesByAdminId(Long adminId);

    List<String> getRoleCodes(Long adminId);
    List<String> getPermissionCodes(Long adminId);
}
