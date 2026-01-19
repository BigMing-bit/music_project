package com.pang.entity.vo;


import lombok.Data;

@Data
public class AppUserVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String coverUrl;
    private String phone;
    private String email;
    private Integer gender;
    private Integer role;

}
