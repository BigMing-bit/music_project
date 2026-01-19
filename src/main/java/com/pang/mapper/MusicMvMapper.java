package com.pang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pang.entity.MusicMv;
import com.pang.entity.vo.AdminMvPageVO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface MusicMvMapper extends BaseMapper<MusicMv> {

    @Select("""
    <script>
    SELECT
      mv.id,
      mv.mv_name AS mvName,
      mv.singer_id AS singerId,
      s.name AS singerName,
      mv.cover_url AS coverUrl,
      mv.video_url AS videoUrl,
      mv.duration_seconds AS durationSeconds,
      mv.play_count AS playCount,
      mv.publish_date AS publishDate,
      mv.sort,
      mv.status,
      mv.create_time AS createTime
    FROM music_mv mv
    LEFT JOIN music_singer s ON s.id = mv.singer_id
    WHERE 1=1
    <if test='keyword!=null and keyword!=\"\"'>
      AND (
        mv.mv_name LIKE CONCAT('%', #{keyword}, '%')
        OR s.name LIKE CONCAT('%', #{keyword}, '%')
      )
    </if>
    <if test='singerId!=null'>
      AND mv.singer_id = #{singerId}
    </if>
    <if test='status!=null'>
      AND mv.status = #{status}
    </if>
    ORDER BY mv.sort DESC, mv.id DESC
    </script>
    """)
    IPage<AdminMvPageVO> adminPage(Page<?> page,
                                   @Param("keyword") String keyword,
                                   @Param("singerId") Long singerId,
                                   @Param("status") Integer status);

    @Select("""
    SELECT
      mv.id,
      mv.mv_name AS mvName,
      mv.singer_id AS singerId,
      s.name AS singerName,
      mv.cover_url AS coverUrl,
      mv.video_url AS videoUrl,
      mv.duration_seconds AS durationSeconds,
      mv.play_count AS playCount,
      mv.publish_date AS publishDate,
      mv.sort,
      mv.status,
      mv.create_time AS createTime
    FROM music_mv mv
    LEFT JOIN music_singer s ON s.id = mv.singer_id
    WHERE mv.id = #{id}
    """)
    AdminMvPageVO adminDetail(@Param("id") Long id);

    @Update("""
    UPDATE music_mv
    SET status = #{status},
        update_time = NOW()
    WHERE id = #{id}
    """)
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
