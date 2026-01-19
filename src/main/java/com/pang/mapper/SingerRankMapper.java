package com.pang.mapper;

import com.pang.entity.vo.SingerRankRowVO;
import org.apache.ibatis.annotations.*;

import java.sql.Date;
import java.util.List;

@Mapper
public interface SingerRankMapper {

    // 1) 清空某个榜单（总榜 rank_date 传 null）
    @Delete("""
        DELETE FROM singer_rank
        WHERE rank_type = #{type}
          AND ((#{rankDate} IS NULL AND rank_date IS NULL) OR rank_date = #{rankDate})
    """)
    void clearRank(@Param("type") Integer type, @Param("rankDate") Date rankDate);

    // 2) 生成榜单：把 singer + hot_score 算出来插入 singer_rank
    @Insert("""
        INSERT INTO singer_rank(rank_type, rank_date, singer_id, hot_score, rank_no)
        SELECT
          #{type} AS rank_type,
          #{rankDate} AS rank_date,
          s.id AS singer_id,
          SUM(COALESCE(song.play_count,0) * #{playW} + COALESCE(song.like_count,0) * #{likeW}) AS hot_score,
          0 AS rank_no
        FROM music_singer s
        LEFT JOIN music_song song ON song.singer_id = s.id AND song.status = 1
        GROUP BY s.id
    """)
    void buildRank(@Param("type") Integer type,
                  @Param("rankDate") Date rankDate,
                  @Param("playW") Integer playW,
                  @Param("likeW") Integer likeW);

    // 3) 更新 rank_no（用 MySQL 8 window function；如果你不是 MySQL8 我再给老写法）
    @Update("""
        UPDATE singer_rank r
        JOIN (
          SELECT id, ROW_NUMBER() OVER (ORDER BY hot_score DESC, singer_id ASC) AS rn
          FROM singer_rank
          WHERE rank_type = #{type}
            AND ((#{rankDate} IS NULL AND rank_date IS NULL) OR rank_date = #{rankDate})
        ) t ON r.id = t.id
        SET r.rank_no = t.rn
    """)
    void refreshRankNo(@Param("type") Integer type, @Param("rankDate") Date rankDate);

    // 4) 前台/后台查询榜单
    @Select("""
        SELECT
          r.rank_no AS rankNo,
          r.singer_id AS singerId,
          s.name AS singerName,
          r.hot_score AS hotScore
        FROM singer_rank r
        JOIN music_singer s ON s.id = r.singer_id
        WHERE r.rank_type = #{type}
          AND ((#{rankDate} IS NULL AND r.rank_date IS NULL) OR r.rank_date = #{rankDate})
        ORDER BY r.rank_no ASC
        LIMIT #{size}
    """)
    List<SingerRankRowVO> top(@Param("type") Integer type,
                              @Param("rankDate") java.sql.Date rankDate,
                              @Param("size") Integer size);
}
