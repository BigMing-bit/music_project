package com.pang.security.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRegisterDTO {

    @NotNull(message = "用户名不能为空")
    private String username;

    @NotNull(message = "密码不能为空")
    private String password;

    @NotNull(message = "昵称不能为空")
    private String nickname;

    @NotNull(message="captchaId不能为空")
    private String captchaId;

    @NotNull(message="captchaCode不能为空")
    private String captchaCode;
    @NotNull(message = "Role cannot be null")
    @Min(value = 0, message = "Role must be 0 (user) or 1 (official)")
    @Max(value = 1, message = "Role must be 0 (user) or 1 (official)")
    private Integer role;  // 角色：0-普通用户，1-官方

}
