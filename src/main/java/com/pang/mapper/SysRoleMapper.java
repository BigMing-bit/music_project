package com.pang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pang.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("""
        select distinct r.role_code
        from sys_role r
        join sys_admin_role ar on ar.role_id = r.id
        where ar.admin_id = #{adminId}
    """)
    List<String> selectRoleCodesByAdminId(@Param("adminId") Long adminId);
}
