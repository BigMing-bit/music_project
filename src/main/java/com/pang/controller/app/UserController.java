package com.pang.controller.app;

import cn.dev33.satoken.SaManager;
import com.pang.common.Result;
import com.pang.common.CommonConstants; // 引入常量类
import com.pang.entity.User;
import com.pang.entity.vo.UserVo;
import com.pang.security.dto.*;
import com.pang.service.UserService;
import com.pang.utils.CaptchaUtil;
import com.pang.utils.MD5Utils;
import com.pang.utils.SaTokenUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/app/user")
public class UserController {

    @Autowired
    UserService userService;

    // 获取用户资料
    @GetMapping
    public Result getProfile() {
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(CommonConstants.NOT_FOUND, "用户不存在");
        }
        UserVo vo = new UserVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setStatus(user.getStatus());
        vo.setRole(user.getRole()); // 添加角色字段
        return Result.success(vo);
    }

    // 用户注册接口
    @PostMapping("/register")
    public Result register(@RequestBody @Valid UserRegisterDTO dto) {
        // 校验验证码
        String rightCode = SaManager.getSaTokenDao().get("captcha:" + dto.getCaptchaId());
        if (rightCode == null || !rightCode.equalsIgnoreCase(dto.getCaptchaCode())) {
            return Result.error(CommonConstants.INVALID_PARAMETER, "验证码错误或已过期");
        }
        // 删除验证码（防重放）
        SaManager.getSaTokenDao().delete("captcha:" + dto.getCaptchaId());

        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(MD5Utils.encode(dto.getPassword())); // 密码加密
        user.setNickname(dto.getNickname());
        user.setRole(dto.getRole());

        // 注册
        boolean isRegistered = userService.register(user);

        return isRegistered ? Result.success("注册成功") : Result.error(CommonConstants.FAIL, "注册失败");
    }

    // 用户登录接口
    @PostMapping("/login")
    public Result login(@RequestBody @Valid UserLoginCaptchaDTO dto) {
        // 校验验证码
        String rightCode = SaManager.getSaTokenDao().get("captcha:" + dto.getCaptchaId());
        if (rightCode == null || !rightCode.equalsIgnoreCase(dto.getCaptchaCode())) {
            return Result.error(CommonConstants.INVALID_PARAMETER, "验证码错误或已过期");
        }
        // 删除验证码（防重放）
        SaManager.getSaTokenDao().delete("captcha:" + dto.getCaptchaId());

        // 调用登录服务
        User user = userService.login(dto.getUsername(), dto.getPassword());
        if (user == null) {
            return Result.error(CommonConstants.LOGIN_FAILED, "用户名或密码错误");
        }

        // 角色验证
        if (dto.getRole() == 1 && user.getRole() != 1) {
            return Result.error(CommonConstants.PERMISSION_DENIED, "只能以普通用户身份登录");
        }
        if (dto.getRole() == 0 && user.getRole() != 0) {
            return Result.error(CommonConstants.PERMISSION_DENIED, "只能以官方身份登录");
        }

        SaTokenUtil.USER.login(user.getId());

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
        data.put("status",  user.getStatus());
        data.put("role", user.getRole()); // 添加角色字段

        return Result.success(data);
    }

    // 修改个人信息
    @PutMapping("/update")
    public Result updateProfile(@RequestBody UserUpdateDTO dto) {
        Long userId = SaTokenUtil.USER.getLoginIdAsLong(); // 从 token 获取用户id

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setNickname(dto.getNickname());
        updateUser.setEmail(dto.getEmail());
        updateUser.setPhone(dto.getPhone());
        updateUser.setAvatarUrl(dto.getAvatarUrl());

        boolean updated = userService.updateById(updateUser);

        return updated ? Result.success("更新成功") : Result.error(CommonConstants.FAIL, "更新失败");
    }

    // 修改密码
    @PutMapping("/change-password")
    public Result changePassword(@RequestBody @Valid ChangePasswordDTO dto) {
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();

        boolean ok = userService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        if (!ok) {
            return Result.error(CommonConstants.INVALID_PARAMETER, "旧密码错误或修改失败");
        }

        // 修改密码成功后强制下线
        SaTokenUtil.USER.logout();
        return Result.success("密码修改成功，请重新登录");
    }

    // 获取验证码
    @GetMapping("/captcha")
    public Result captcha() {
        // 生成验证码
        String code = CaptchaUtil.randomCode(4);
        String captchaId = java.util.UUID.randomUUID().toString();

        // 存储验证码到 SaTokenDao
        SaManager.getSaTokenDao().set("captcha:" + captchaId, code, 120);

        // 生成图片
        String img = CaptchaUtil.generateImageBase64(code);

        Map<String, Object> data = new HashMap<>();
        data.put("captchaId", captchaId);
        data.put("img", img);

        return Result.success(data);
    }

    // 用户登出
    @PostMapping("/logout")
    public Result logout() {
        SaTokenUtil.USER.logout();
        return Result.success("已退出");
    }
}
