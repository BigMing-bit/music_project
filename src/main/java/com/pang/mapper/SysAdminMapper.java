package com.pang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pang.entity.SysAdmin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysAdminMapper extends BaseMapper<SysAdmin> {

    @Select("SELECT COALESCE(display_name, username) FROM sys_admin WHERE id = #{id}")
    String selectDisplayName(@Param("id") Long id);
}
