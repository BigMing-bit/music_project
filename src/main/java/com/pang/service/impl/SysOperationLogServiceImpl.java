package com.pang.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.SysOperationLog;
import com.pang.mapper.SysOperationLogMapper;
import com.pang.security.dto.LogQueryDTO;
import com.pang.service.SysOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements SysOperationLogService {

    private final SysOperationLogMapper logMapper;

    @Override
    public IPage<SysOperationLog> adminPage(LogQueryDTO queryDTO) {
        Page<SysOperationLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        return logMapper.selectAdminPage(page, queryDTO.getKeyword(), queryDTO.getSuccess());
    }
}
