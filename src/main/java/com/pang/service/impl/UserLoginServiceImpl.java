package com.pang.service.impl;

import com.pang.common.constants.CommonConstants;
import com.pang.common.Result;
import com.pang.entity.User;
import com.pang.security.dto.UserLoginCaptchaDTO;
import com.pang.service.UserLoginService;
import com.pang.service.UserService;
import com.pang.utils.SaTokenUtil;
import cn.dev33.satoken.SaManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserLoginServiceImpl implements UserLoginService {

    private final UserService userService;

    public UserLoginServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Result login(UserLoginCaptchaDTO dto, HttpServletRequest request) {
        try {
            // 校验验证码
            if (!validateCaptcha(dto.getCaptchaId(), dto.getCaptchaCode())) {
                return Result.error(CommonConstants.INVALID_PARAMETER, "验证码错误或已过期");
            }

            // 调用用户服务进行认证
            User user = userService.authenticate(dto.getUsername(), dto.getPassword());
            if (user == null) {
                return Result.error(CommonConstants.LOGIN_FAILED, "用户名或密码错误");
            }

            // 角色验证
            if (!validateRole(user, dto.getRole())) {
                return Result.error(CommonConstants.PERMISSION_DENIED, "角色验证失败");
            }

            // 登录成功，生成token
            SaTokenUtil.USER.login(user.getId());

            // 构建登录响应数据
            Map<String, Object> data = buildLoginResponseData(user);

            return Result.success(data);
        } catch (Exception e) {
            return Result.error(CommonConstants.LOGIN_FAILED, e.getMessage());
        }
    }

    @Override
    public Result logout() {
        SaTokenUtil.USER.logout();
        return Result.success("已退出");
    }

    @Override
    public Map<String, Object> buildLoginResponseData(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("tokenName", SaTokenUtil.USER.getTokenName());
        data.put("tokenValue", SaTokenUtil.USER.getTokenValue());
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("email", user.getEmail());
        data.put("gender", user.getGender());
        data.put("avatarUrl", user.getAvatarUrl());
        data.put("phone", user.getPhone());
        data.put("status", user.getStatus());
        data.put("role", user.getRole());
        return data;
    }

    /**
     * 校验验证码
     */
    private boolean validateCaptcha(String captchaId, String captchaCode) {
        String rightCode = SaManager.getSaTokenDao().get("captcha:" + captchaId);
        if (rightCode == null || !rightCode.equalsIgnoreCase(captchaCode)) {
            return false;
        }
        // 删除验证码（防重放）
        SaManager.getSaTokenDao().delete("captcha:" + captchaId);
        return true;
    }

    /**
     * 校验角色
     */
    private boolean validateRole(User user, Integer role) {
        if (role == 1 && user.getRole() != 1) {
            return false;
        }
        if (role == 0 && user.getRole() != 0) {
            return false;
        }
        return true;
    }
}
