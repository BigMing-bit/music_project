package com.pang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.entity.Banner;

public interface BannerService extends IService<Banner> {
    IPage<Banner> adminPage(Integer pageNum, Integer pageSize, String keyword, Integer status);
}
