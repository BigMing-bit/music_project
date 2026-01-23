package com.pang.service;

import com.pang.entity.User;
import com.pang.security.dto.UserLoginCaptchaDTO;


public interface UserLoginService extends LoginService<User, UserLoginCaptchaDTO> {
    // 可以添加用户登录特有的方法
}
