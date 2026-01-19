package com.pang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.vo.DashboardVo;
import com.pang.mapper.DashboardMapper;
import com.pang.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardServiceImpl extends ServiceImpl<DashboardMapper, DashboardVo> implements DashboardService {

    private final DashboardMapper dashboardMapper;

    public DashboardServiceImpl(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    @Override
    public DashboardVo getDashboard() {

        DashboardVo vo = new DashboardVo();

        // ✅ 统计
        DashboardVo.Stats stats = dashboardMapper.stats();
        if (stats == null) stats = new DashboardVo.Stats();
        vo.setStats(stats);

        // ✅ 趋势（补齐空天）
        List<DashboardVo.TrendPoint> raw = dashboardMapper.playTrend7d();
        Map<String, Long> map = new HashMap<>();

        if (raw != null) {
            for (DashboardVo.TrendPoint p : raw) {
                map.put(p.getDay(), p.getCount());
            }
        }

        List<DashboardVo.TrendPoint> filled = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.now().minusDays(6);

        for (int i = 0; i < 7; i++) {
            String day = start.plusDays(i).format(fmt);
            filled.add(new DashboardVo.TrendPoint(day, map.getOrDefault(day, 0L)));
        }

        vo.setPlayTrend7d(filled);

        // ✅ 最近新增歌曲 / 最近日志
        vo.setLatestSongs(Optional.ofNullable(dashboardMapper.latestSongs()).orElse(Collections.emptyList()));
        vo.setLatestLogs(Optional.ofNullable(dashboardMapper.latestLogs()).orElse(Collections.emptyList()));

        return vo;
    }
}
