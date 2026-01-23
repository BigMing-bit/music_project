package com.pang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.entity.SysOperationLog;
import com.pang.security.dto.LogQueryDTO;

public interface SysOperationLogService extends IService<SysOperationLog> {
    IPage<SysOperationLog> adminPage(LogQueryDTO queryDTO);
}
