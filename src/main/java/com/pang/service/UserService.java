package com.pang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.entity.User;

public interface UserService extends IService<User> {

    User getById(Long id);
    boolean register(User user);  // 注册
    User login(String username, String password);  // 登录
    boolean updateProfile(User user);  // 修改个人信息

    boolean changePassword(Long userId, String oldPassword, String newPassword);


   
    long count();

    IPage<User> adminPage(Integer pageNum, Integer pageSize, String keyword, Integer status, Integer gender);
}
