package com.pang.config;

import cn.dev33.satoken.stp.StpInterface;
import com.pang.utils.SaTokenUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限验证接口实现
 */
@Component
public class SaTokenStpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {

        // ✅ 只处理 admin 体系
        if (!"admin".equals(loginType)) {
            return List.of();
        }

        Object perms = SaTokenUtil.ADMIN
                .getSessionByLoginId(loginId)
                .get("permissions");

        System.out.println("loginType=" + loginType + ", perms=" + perms);


        return perms == null ? List.of() : (List<String>) perms;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {

        // ✅ 只处理 admin 体系
        if (!"admin".equals(loginType)) {
            return List.of();
        }

        Object roles = SaTokenUtil.ADMIN
                .getSessionByLoginId(loginId)
                .get("roles");

        return roles == null ? List.of() : (List<String>) roles;
    }
}


