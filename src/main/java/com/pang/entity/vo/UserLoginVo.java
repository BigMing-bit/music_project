package com.pang.entity.vo;

import lombok.Data;

@Data
public class UserLoginVo {
    private Long id;

    private String username;  // 用户名（唯一）
    private String password;  // 密码（加密存储）
    private String nickname;  // 昵称
    private String avatarUrl; // 头像URL
    private String phone;     // 手机号
    private String token;
    private Long tokenExpireTime;
    private String email;     // 邮箱
    private Integer status;   // 状态：0禁用 1正常


}
