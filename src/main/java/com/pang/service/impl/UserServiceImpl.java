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
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper UserMapper;

    public UserServiceImpl(UserMapper UserMapper) {
        this.UserMapper = UserMapper;
    }

    @Override
    public User getById(Long id) {
        return UserMapper.selectById(id);
    }

    @Override
    public boolean register(User user) {
        // 检查用户名是否已存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        User existingUser = UserMapper.selectOne(wrapper);
        if (existingUser != null) {
            return false;  // 用户名已存在
        }
        // 保存用户
        return UserMapper.insert(user) > 0;
    }

    @Override
    public User login(String username, String password) {
        User user = UserMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) return null;

        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!md5Password.equals(user.getPassword())) return null;

        return user; // ✅ 不要在 service 里 login
    }


    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {

        User user = this.getById(userId);
        if (user == null) return false;

        // ✅ 校验旧密码
        String oldMd5 = MD5Utils.encode(oldPassword);
        if (!oldMd5.equals(user.getPassword())) {
            return false;
        }

        // ✅ 更新新密码
        User update = new User();
        update.setId(userId);
        update.setPassword(MD5Utils.encode(newPassword));

        return this.updateById(update);
    }


    @Override
    public boolean updateProfile(User user) {
        // 更新用户信息
        return UserMapper.updateById(user) > 0;
    }
    
    @Override
    public long count() {
        return UserMapper.selectCount(null);
    }


    @Override
    public IPage<User> adminPage(Integer pageNum, Integer pageSize, String keyword, Integer status, Integer gender) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;

        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.like(StrUtil.isNotBlank(keyword), User::getUsername, keyword)
                .or(StrUtil.isNotBlank(keyword)).like(StrUtil.isNotBlank(keyword), User::getNickname, keyword)
                .eq(status != null, User::getStatus, status)
                .eq(gender != null, User::getGender, gender)
                .orderByDesc(User::getId);

        return this.page(page, qw);
    }


}
