package com.pang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_admin_role")
public class SysAdminRole {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long adminId;
    private Long roleId;
    private LocalDateTime createTime;
}
