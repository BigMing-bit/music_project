package com.pang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.entity.User;

public interface UserService extends IService<User> {

    boolean register(User user);
    User authenticate(String username, String password);
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    IPage<User> adminPage(Integer pageNum, Integer pageSize, String keyword, Integer status, Integer gender);
}
