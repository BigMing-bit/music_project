package com.pang.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.User;
import com.pang.mapper.UserMapper;
import com.pang.service.UserService;
import com.pang.utils.MD5Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;

    @Override
    public boolean register(User user) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername());
        return this.count(qw) == 0 && this.save(user);
    }

    @Override
    public User authenticate(String username, String password) {
        User user = this.lambdaQuery()
                .eq(User::getUsername, username)
                .one();
        
        if (user == null) return null;
        
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        return md5Password.equals(user.getPassword()) ? user : null;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) return false;

        String oldMd5 = MD5Utils.encode(oldPassword);
        if (!oldMd5.equals(user.getPassword())) {
            return false;
        }

        return this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getPassword, MD5Utils.encode(newPassword))
                .update();
    }

    @Override
    public IPage<User> adminPage(Integer pageNum, Integer pageSize, String keyword, Integer status, Integer gender) {
        pageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        pageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;

        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<User>()
                .like(StrUtil.isNotBlank(keyword), User::getUsername, keyword)
                .or(StrUtil.isNotBlank(keyword)).like(StrUtil.isNotBlank(keyword), User::getNickname, keyword)
                .eq(status != null, User::getStatus, status)
                .eq(gender != null, User::getGender, gender)
                .orderByDesc(User::getId);

        return this.page(page, qw);
    }
}
