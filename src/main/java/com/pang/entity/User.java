package com.pang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("app_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;  // 用户名（唯一）
    private String password;  // 密码（加密存储）
    private String nickname;  // 昵称
    private String avatarUrl; // 头像URL
    @TableField("cover_url")   // ✅ 新增
    private String coverUrl;
    private String phone;     // 手机号
    private Integer gender;   // 性别
    private String email;     // 邮箱
    private Integer status;   // 状态：0禁用 1正常
    private LocalDateTime createTime;  // 创建时间
    private LocalDateTime updateTime;  // 更新时间
    private Integer role;

}
