package com.pang.service;

import com.pang.common.Result;
import com.pang.entity.SysAdmin;
import com.pang.security.dto.AdminLoginDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface AdminLoginService extends LoginService<SysAdmin, AdminLoginDTO> {
    // 专注于登录流程，角色权限查询由 AdminAuthService 负责
}
