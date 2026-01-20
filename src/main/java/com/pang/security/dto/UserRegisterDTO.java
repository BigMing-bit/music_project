package com.pang.security.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRegisterDTO {

    @NotNull(message = "用户名不能为空")
    private String username;

    @NotNull(message = "密码不能为空")
    private String password;

    private String nickname;

    @NotNull(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotNull(message = "短信验证码不能为空")
    private String smsCode;

    @NotNull(message="captchaId不能为空")
    private String captchaId;

    @NotNull(message="captchaCode不能为空")
    private String captchaCode;

    @NotNull(message = "Role cannot be null")
    @Min(value = 0, message = "Role must be 0 (user) or 1 (official)")
    @Max(value = 1, message = "Role must be 0 (user) or 1 (official)")
    private Integer role;  // 角色：0-普通用户，1-官方

}
