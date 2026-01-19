package com.pang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.entity.SysOperationLog;

public interface SysOperationLogService extends IService<SysOperationLog> {
    IPage<SysOperationLog> adminPage(Integer pageNum, Integer pageSize,
                                     String keyword, Integer success);

}
