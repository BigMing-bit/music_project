package com.pang.security.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserLoginCaptchaDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "验证码ID不能为空")
    private String captchaId; // ✅ 验证码ID

    @NotBlank(message = "验证码不能为空")
    private String captchaCode; // ✅ 用户输入的验证码

    @NotNull(message = "Role cannot be null")
    private Integer role;  // 角色：0-普通用户，1-官方
}
