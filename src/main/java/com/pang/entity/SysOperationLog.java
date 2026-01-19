package com.pang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("admin_id")
    private Long adminId;

    @TableField("admin_username")
    private String adminUsername;

    private String module; //模块

    private String action; // 动作

    private String method;  // GET/POST/PUT/DELETE

    private String path;  // /admin/singers/list

    private String params;  //请求参数（JSON字符串）

    private String ip;   // IP

    private Integer success;  // 1成功 0失败

    @TableField("error_msg")
    private String errorMsg;   // 异常信息

    @TableField("create_time")
    private LocalDateTime createTime;
}
