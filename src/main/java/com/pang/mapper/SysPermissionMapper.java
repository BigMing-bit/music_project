package com.pang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pang.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    @Select("""
        select distinct p.permission_code
        from sys_permission p
        join sys_role_permission rp on rp.permission_id = p.id
        join sys_admin_role ar on ar.role_id = rp.role_id
        where ar.admin_id = #{adminId}
    """)
    List<String> selectPermCodesByAdminId(@Param("adminId") Long adminId);


}
