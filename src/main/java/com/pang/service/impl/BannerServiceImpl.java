package com.pang.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.Banner;
import com.pang.mapper.BannerMapper;
import com.pang.service.BannerService;
import org.springframework.stereotype.Service;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Override
    public IPage<Banner> adminPage(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;

        Page<Banner> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Banner> qw = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(keyword)) {
            qw.and(w -> w.like(Banner::getTitle, keyword)
                    .or().like(Banner::getJumpTarget, keyword));
        }
        qw.eq(status != null, Banner::getStatus, status);

        // 常见排序：sort desc, id desc
        qw.orderByDesc(Banner::getSort).orderByDesc(Banner::getId);

        return this.page(page, qw);
    }
}
