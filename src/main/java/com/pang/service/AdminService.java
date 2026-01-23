package com.pang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.entity.SysAdmin;
import com.pang.entity.SysRole;
import com.pang.entity.vo.AdminVO;
import com.pang.security.dto.AdminQueryDTO;

import java.util.List;

public interface AdminService extends IService<SysAdmin> {
    IPage<AdminVO> adminPage(AdminQueryDTO queryDTO);
    List<SysRole> getRolesByAdminId(Long adminId);
    void saveAdminRoles(Long adminId, List<Long> roleIds);
}


