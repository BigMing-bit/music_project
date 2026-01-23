package com.pang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.SysAdmin;
import com.pang.entity.SysAdminRole;
import com.pang.entity.SysRole;
import com.pang.entity.vo.AdminVO;
import com.pang.mapper.SysAdminMapper;
import com.pang.mapper.SysAdminRoleMapper;
import com.pang.mapper.SysRoleMapper;
import com.pang.security.dto.AdminQueryDTO;
import com.pang.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl extends ServiceImpl<SysAdminMapper, SysAdmin> implements AdminService {

    private final SysAdminMapper adminMapper;
    private final SysAdminRoleMapper adminRoleMapper;
    private final SysRoleMapper roleMapper;

    @Override
    public IPage<AdminVO> adminPage(AdminQueryDTO queryDTO) {
        Page<SysAdmin> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<SysAdmin> sysAdminPage = adminMapper.selectAdminPage(page, queryDTO.getKeyword(), queryDTO.getStatus());
        
        return sysAdminPage.convert(sysAdmin -> {
            AdminVO adminVO = new AdminVO();
            BeanUtils.copyProperties(sysAdmin, adminVO);
            adminVO.setRoles(getRolesByAdminId(sysAdmin.getId()));
            return adminVO;
        });
    }

    @Override
    public List<SysRole> getRolesByAdminId(Long adminId) {
        LambdaQueryWrapper<SysAdminRole> qw = new LambdaQueryWrapper<>();
        qw.eq(SysAdminRole::getAdminId, adminId);
        List<SysAdminRole> adminRoles = adminRoleMapper.selectList(qw);
        
        if (adminRoles.isEmpty()) {
            return List.of();
        }
        
        List<Long> roleIds = adminRoles.stream().map(SysAdminRole::getRoleId).toList();
        LambdaQueryWrapper<SysRole> roleQw = new LambdaQueryWrapper<>();
        roleQw.in(SysRole::getId, roleIds);
        return roleMapper.selectList(roleQw);
    }

    @Override
    @Transactional
    public void saveAdminRoles(Long adminId, List<Long> roleIds) {
        // Delete existing roles
        LambdaQueryWrapper<SysAdminRole> deleteQw = new LambdaQueryWrapper<>();
        deleteQw.eq(SysAdminRole::getAdminId, adminId);
        adminRoleMapper.delete(deleteQw);
        
        // Save new roles
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysAdminRole adminRole = new SysAdminRole();
                adminRole.setAdminId(adminId);
                adminRole.setRoleId(roleId);
                adminRole.setCreateTime(LocalDateTime.now());
                adminRoleMapper.insert(adminRole);
            }
        }
    }

}
