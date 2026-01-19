package com.pang.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.SysAdmin;
import com.pang.mapper.SysAdminMapper;
import com.pang.service.AdminService;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<SysAdminMapper, SysAdmin> implements AdminService {

    private final SysAdminMapper adminMapper;

    public AdminServiceImpl(SysAdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    @Override
    public SysAdmin getById(Long id) {
        return adminMapper.selectById(id);
    }

    @Override
    public boolean updateById(SysAdmin admin) {
        return adminMapper.updateById(admin) > 0;  // MyBatis-Plus 的 updateById 方法，返回更新的行数
    }

    @Override
    public IPage<SysAdmin> adminPage(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;

        Page<SysAdmin> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<SysAdmin> qw = new LambdaQueryWrapper<>();

        qw.select(
                SysAdmin::getId,
                SysAdmin::getUsername,
                SysAdmin::getNickname,
                SysAdmin::getAvatarUrl,
                SysAdmin::getStatus,
                SysAdmin::getLastLoginTime,
                SysAdmin::getCreateTime,
                SysAdmin::getUpdateTime
        );

        qw.and(StrUtil.isNotBlank(keyword), w -> w
                        .like(SysAdmin::getUsername, keyword)
                        .or()
                        .like(SysAdmin::getNickname, keyword)
                )
                .eq(status != null, SysAdmin::getStatus, status)
                .orderByDesc(SysAdmin::getId);

        return this.page(page, qw);
    }

}
