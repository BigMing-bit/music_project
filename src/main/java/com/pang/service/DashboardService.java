package com.pang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.entity.vo.DashboardVo;

public interface DashboardService extends IService<DashboardVo> {
    DashboardVo getDashboard();
}
