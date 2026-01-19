package com.pang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pang.entity.vo.DashboardVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DashboardMapper extends BaseMapper<DashboardVo> {

    // ✅ 统计：一次查出多个数字（推荐）
    @Select("""
        SELECT
          (SELECT COUNT(*) FROM app_user) AS userCount,
          (SELECT COUNT(*) FROM music_song) AS songCount,
          (SELECT COUNT(*) FROM music_album) AS albumCount,
          (SELECT COUNT(*) FROM music_singer) AS singerCount,
          (SELECT COUNT(*) FROM music_comment) AS commentTotal,
          (SELECT COUNT(*) FROM music_play_history) AS playCountTotal,
          (SELECT COUNT(*) FROM music_song_like) AS likeCountTotal,
          (SELECT COUNT(*) FROM music_favorite_playlist) AS favPlaylistTotal,
          (SELECT COUNT(*) FROM music_play_history WHERE play_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)) AS playCount7d
    """)
    DashboardVo.Stats stats();

    // ✅ 7天播放趋势
    @Select("""
       SELECT DATE(play_time) AS day, COUNT(*) AS count
       FROM music_play_history
       WHERE play_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
       GROUP BY DATE(play_time)
       ORDER BY day
       
    """)
    List<DashboardVo.TrendPoint> playTrend7d();

    // ✅ 最近新增歌曲（带 singerName）
    @Select("""
        SELECT
          s.id AS id,
          s.song_name AS songName,
          si.name AS singerName,
          s.play_count AS playCount,
          DATE_FORMAT(s.create_time, '%Y-%m-%d %H:%i:%s') AS createTime
        FROM music_song s
        LEFT JOIN music_singer si ON si.id = s.singer_id
        ORDER BY s.create_time DESC
        LIMIT 10
    """)
    List<DashboardVo.LatestSong> latestSongs();

    // ✅ 最近操作日志
    @Select("""
        SELECT
          id,
          module,
          admin_username,
          method,
          ip,
          path,
          action,
          success,
          DATE_FORMAT(create_time, '%Y-%m-%d %H:%i:%s') AS createTime
        FROM sys_operation_log
        ORDER BY create_time DESC
        LIMIT 10
    """)
    List<DashboardVo.OpLog> latestLogs();
}
