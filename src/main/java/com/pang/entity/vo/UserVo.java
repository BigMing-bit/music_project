package com.pang.entity.vo;

import lombok.Data;

@Data
public class UserVo {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatarUrl;
    private Integer status;
    private Integer role;
}
