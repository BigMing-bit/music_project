package com.pang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.entity.SysAdmin;

public interface AdminService extends IService<SysAdmin> {
    SysAdmin getById(Long id);
    boolean updateById(SysAdmin admin);

    IPage<SysAdmin> adminPage(Integer pageNum, Integer pageSize, String keyword, Integer status);

}

