package com.pang.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.util.List;

@Data
public class DashboardVo {

    private Stats stats;
    private List<TrendPoint> playTrend7d;
    private List<LatestSong> latestSongs;
    private List<OpLog> latestLogs;

    @Data
    public static class Stats {
        private Long userCount;
        private Long songCount;
        private Long albumCount;
        private Long singerCount;

        private Long playCount7d;      // 最近7天播放
        private Long playCountTotal;   // 总播放（按 play_history）
        private Long likeCountTotal;   // 总点赞（按 song_like）
        private Long favPlaylistTotal; // 歌单收藏（favorite_playlist）
        private Long commentTotal;     // 评论数
    }

    @Data
    public static class TrendPoint {
        private String day;   // yyyy-MM-dd
        private Long count;
        public TrendPoint(String day, Long count) { this.day = day; this.count = count; }
    }

    @Data
    public static class LatestSong {

        @TableId(type = IdType.AUTO)
        private Long id;
        private String songName;
        private String singerName;
        private Long playCount;     // 如果 music_song 有 play_count 就返回；没有就返回 null
        private String createTime;  // yyyy-MM-dd HH:mm:ss
    }

    @Data
    public static class OpLog {
        @TableId(type = IdType.AUTO)
        private Long id;
        private String adminUserName;
        private String module;
        private String action;
        private String method;
        private String ip;
        private String path;
        private String success;
        private String createTime;
    }
}
