package com.pang.controller.app;

import cn.dev33.satoken.SaManager;
import com.pang.common.Result;
import com.pang.common.constants.CommonConstants;
import com.pang.common.utils.SmsUtil;
import com.pang.entity.User;
import com.pang.entity.vo.UserVo;
import com.pang.security.dto.*;
import com.pang.service.UserLoginService;
import com.pang.service.UserService;
import com.pang.utils.CaptchaUtil;
import com.pang.utils.MD5Utils;
import com.pang.utils.SaTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/app/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserLoginService userLoginService;
    private final SmsUtil smsUtil;

    // 获取用户资料
    @GetMapping
    public Result getProfile() {
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(CommonConstants.NOT_FOUND, "用户不存在");
        }
        UserVo vo = new UserVo();
        BeanUtils.copyProperties(user, vo);
        return Result.success(vo);
}


    @PostMapping("/send-sms")
    public Result sendSmsCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        if (phone == null || phone.trim().isEmpty()) {
            return Result.error(CommonConstants.INVALID_PARAMETER, "手机号不能为空");
        }
        String code = SmsUtil.generateVerifyCode();
        boolean success = smsUtil.sendSmsCode(phone, code);
        if (!success) {
            return Result.error(CommonConstants.FAIL, "短信发送失败，请稍后重试");
        }
        SaManager.getSaTokenDao().set("sms:code:" + phone, code, 300);
        
        return Result.success("验证码发送成功");
    }

    // 用户注册接口
    @PostMapping("/register")
    public Result register(@RequestBody @Valid UserRegisterDTO dto) {
        // 校验图形验证码
        String rightCode = SaManager.getSaTokenDao().get("captcha:" + dto.getCaptchaId());
        if (rightCode == null || !rightCode.equalsIgnoreCase(dto.getCaptchaCode())) {
            return Result.error(CommonConstants.INVALID_PARAMETER, "图形验证码错误或已过期");
        }
        SaManager.getSaTokenDao().delete("captcha:" + dto.getCaptchaId());

        // 校验短信验证码
        String rightSmsCode = SaManager.getSaTokenDao().get("sms:code:" + dto.getPhone());
        if (rightSmsCode == null || !rightSmsCode.equals(dto.getSmsCode())) {
            return Result.error(CommonConstants.INVALID_PARAMETER, "短信验证码错误或已过期");
        }
        // 删除短信验证码（防重放）
        SaManager.getSaTokenDao().delete("sms:code:" + dto.getPhone());

        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(MD5Utils.encode(dto.getPassword())); // 密码加密
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());

        // 注册
        boolean isRegistered = userService.register(user);

        return isRegistered ? Result.success("注册成功") : Result.error(CommonConstants.FAIL, "注册失败");
    }

    // 用户登录接口
    @PostMapping("/login")
    public Result login(@RequestBody @Valid UserLoginCaptchaDTO dto, HttpServletRequest request) {
        return userLoginService.login(dto, request);
    }

    // 修改个人信息
    @PutMapping("/update")
    public Result updateProfile(@RequestBody UserUpdateDTO dto) {
        User user = new User();
        user.setId(SaTokenUtil.USER.getLoginIdAsLong());
        BeanUtils.copyProperties(dto, user, "id");

        boolean updated = userService.updateById(user);
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
        SaTokenUtil.USER.logout();
        return Result.success("密码修改成功，请重新登录");
    }
    @GetMapping("/captcha")
    public Result captcha() {
        String code = CaptchaUtil.randomCode(4);
        String captchaId = java.util.UUID.randomUUID().toString();
        SaManager.getSaTokenDao().set("captcha:" + captchaId, code, 120);

        Map<String, Object> data = new HashMap<>();
        data.put("captchaId", captchaId);
        data.put("img", CaptchaUtil.generateImageBase64(code));
        return Result.success(data);
}

    // 用户登出
    @PostMapping("/logout")
    public Result logout() {
        return userLoginService.logout();
    }
}
