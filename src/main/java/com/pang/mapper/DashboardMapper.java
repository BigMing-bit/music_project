package com.pang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pang.entity.vo.DashboardVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DashboardMapper extends BaseMapper<DashboardVo> {

    // ✅ 统计：一次查出多个数字（推荐）
    DashboardVo.Stats stats();

    // ✅ 7天播放趋势
    List<DashboardVo.TrendPoint> playTrend7d();

    // ✅ 最近新增歌曲（带 singerName）
    List<DashboardVo.LatestSong> latestSongs();

    // ✅ 最近操作日志
    List<DashboardVo.OpLog> latestLogs();
}
