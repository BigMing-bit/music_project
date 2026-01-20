package com.pang.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.SysOperationLog;
import com.pang.mapper.SysOperationLogMapper;
import com.pang.service.SysOperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements SysOperationLogService {

    @Override
    public IPage<SysOperationLog> adminPage(Integer pageNum, Integer pageSize, String keyword, Integer success) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;

        Page<SysOperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysOperationLog> qw = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(keyword)) {
            qw.and(w -> w.like(SysOperationLog::getAdminUsername, keyword)
                    .or().like(SysOperationLog::getModule, keyword)
                    .or().like(SysOperationLog::getAction, keyword)
                    .or().like(SysOperationLog::getPath, keyword)
                    .or().like(SysOperationLog::getIp, keyword));
        }
        qw.eq(success != null, SysOperationLog::getSuccess, success);

        qw.orderByDesc(SysOperationLog::getId);
        return this.page(page, qw);
    }

}
