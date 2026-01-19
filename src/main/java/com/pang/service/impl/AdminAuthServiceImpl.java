package com.pang.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pang.common.CommonConstants;
import com.pang.entity.*;
import com.pang.exception.BizException;
import com.pang.mapper.*;
import com.pang.service.AdminAuthService;
import com.pang.utils.SaTokenUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private final SysAdminMapper adminMapper;
    private final SysAdminRoleMapper adminRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRoleMapper roleMapper;

    public AdminAuthServiceImpl(SysAdminMapper adminMapper,
                                SysAdminRoleMapper adminRoleMapper,
                                SysRolePermissionMapper rolePermissionMapper,
                                SysPermissionMapper permissionMapper,
                                SysRoleMapper roleMapper) {
        this.adminMapper = adminMapper;
        this.adminRoleMapper = adminRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public SysAdmin login(String username, String password) {
        SysAdmin admin = adminMapper.selectOne(new QueryWrapper<SysAdmin>().eq("username", username));

        if (admin == null) {
            throw new BizException(CommonConstants.LOGIN_FAILED, "用户名或密码错误");
        }
        if (admin.getStatus() != 1) {
            throw new BizException(CommonConstants.ACCOUNT_DISABLED, "账号被封禁");
        }

        String md5 = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!md5.equals(admin.getPassword())) {
            throw new BizException(CommonConstants.LOGIN_FAILED, "用户名或密码错误");
        }
        // 登录成功，绑定 loginId
        SaTokenUtil.ADMIN.login(admin.getId());
        // 绑定角色
        List<String> roles = getRolesByAdminId(admin.getId());
        SaTokenUtil.ADMIN.getSession().set("roles", roles);

        // 绑定权限
        List<String> permissions = getPermissionsByAdminId(admin.getId());
        SaTokenUtil.ADMIN.getSession().set("permissions", permissions);

        return admin;
    }

    @Override
    public List<String> getRolesByAdminId(Long adminId) {
        List<Long> roleIds = adminRoleMapper.selectList(
                new QueryWrapper<SysAdminRole>().eq("admin_id", adminId)
        ).stream().map(SysAdminRole::getRoleId).toList();

        if (roleIds.isEmpty()) return List.of();

        return roleMapper.selectList(
                new QueryWrapper<SysRole>().in("id", roleIds)
        ).stream().map(SysRole::getRoleCode).toList();
    }

    @Override
    public List<String> getPermissionsByAdminId(Long adminId) {
        // 获取角色 ID
        List<Long> roleIds = adminRoleMapper.selectList(
                new QueryWrapper<SysAdminRole>().eq("admin_id", adminId)
        ).stream().map(SysAdminRole::getRoleId).toList();

        if (roleIds.isEmpty()) return List.of();

        // 获取权限 ID
        List<Long> permissionIds = rolePermissionMapper.selectList(
                new QueryWrapper<SysRolePermission>().in("role_id", roleIds)
        ).stream().map(SysRolePermission::getPermissionId).toList();

        if (permissionIds.isEmpty()) return List.of();

        // 返回权限标识
        return permissionMapper.selectList(
                new QueryWrapper<SysPermission>().in("id", permissionIds)
        ).stream().map(SysPermission::getPermissionCode).toList();
    }


    @Override
    public List<String> getRoleCodes(Long adminId) {
        return roleMapper.selectRoleCodesByAdminId(adminId);
    }

    @Override
    public List<String> getPermissionCodes(Long adminId) {
        return permissionMapper.selectPermCodesByAdminId(adminId);
    }
}
