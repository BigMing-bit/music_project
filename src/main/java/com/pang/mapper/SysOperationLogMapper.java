package com.pang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pang.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {

    IPage<SysOperationLog> selectAdminPage(Page<SysOperationLog> page,
                                          @Param("keyword") String keyword,
                                          @Param("success") Integer success);
}
