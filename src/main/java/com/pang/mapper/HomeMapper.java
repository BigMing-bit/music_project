package com.pang.mapper;

import com.pang.entity.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HomeMapper {

    // ===================== 热门歌单 =====================
    @Select("""
    <script>
    SELECT
      p.id,
      p.name,
      p.description,
      p.cover_url AS coverUrl,
      p.play_count AS playCount,
      p.collect_count AS collectCount,
      (IFNULL(p.play_count,0) + IFNULL(p.collect_count,0) * 5) AS score
    FROM music_playlist p
    WHERE p.status = 1
    <if test="cursorScore != null and cursorId != null">
      AND (
        (IFNULL(p.play_count,0) + IFNULL(p.collect_count,0) * 5) &lt; #{cursorScore}
        OR (
          (IFNULL(p.play_count,0) + IFNULL(p.collect_count,0) * 5) = #{cursorScore}
          AND p.id &lt; #{cursorId}
        )
      )
    </if>
    ORDER BY score DESC, p.id DESC
    LIMIT #{size}
    </script>
    """)
    List<PlaylistHomeVo> hotPlaylists(@Param("cursorScore") Long cursorScore,
                                      @Param("cursorId") Long cursorId,
                                      @Param("size") Integer size);


    // ===================== 热门专辑 =====================
    @Select("""
    <script>
    SELECT
      a.id,
      a.album_name AS albumName,
      a.singer_id AS singerId,
      s.name AS singerName,
      a.cover_url AS coverUrl,
      a.collect_count AS collectCount,
      IFNULL(a.collect_count,0) AS score
    FROM music_album a
    LEFT JOIN music_singer s ON s.id = a.singer_id
    WHERE a.status = 1
    <if test="cursorScore != null and cursorId != null">
      AND (
        IFNULL(a.collect_count,0) &lt; #{cursorScore}
        OR (
          IFNULL(a.collect_count,0) = #{cursorScore}
          AND a.id &lt; #{cursorId}
        )
      )
    </if>
    ORDER BY score DESC, a.id DESC
    LIMIT #{size}
    </script>
    """)
    List<AlbumHomeVo> hotAlbums(@Param("cursorScore") Long cursorScore,
                                @Param("cursorId") Long cursorId,
                                @Param("size") Integer size);


    // ===================== 热门歌曲 =====================
    @Select("""
    <script>
    SELECT
      so.id,
      so.song_name AS songName,
      so.singer_id AS singerId,
      si.name AS singerName,
      so.album_id AS albumId,
      so.duration_seconds AS durationSeconds,
      so.cover_url AS coverUrl,
      so.audio_url AS audioUrl,
      so.play_count AS playCount,
      so.like_count AS likeCount,
      (IFNULL(so.play_count,0) + IFNULL(so.like_count,0) * 3) AS score
    FROM music_song so
    LEFT JOIN music_singer si ON si.id = so.singer_id
    WHERE so.status = 1
    <if test="cursorScore != null and cursorId != null">
      AND (
        (IFNULL(so.play_count,0) + IFNULL(so.like_count,0) * 3) &lt; #{cursorScore}
        OR (
          (IFNULL(so.play_count,0) + IFNULL(so.like_count,0) * 3) = #{cursorScore}
          AND so.id &lt; #{cursorId}
        )
      )
    </if>
    ORDER BY score DESC, so.id DESC
    LIMIT #{size}
    </script>
    """)
    List<SongHomeVo> hotSongs(@Param("cursorScore") Long cursorScore,
                              @Param("cursorId") Long cursorId,
                              @Param("size") Integer size);


    // ===================== 轮播图 banners =====================
    @Select("""
    <script>
    SELECT
      b.id,
      b.title,
      b.image_url AS imageUrl,
      b.sort,
      IFNULL(b.sort,0) AS score
    FROM music_banner b
    WHERE b.status = 1
    <if test="cursorScore != null and cursorId != null">
      AND (
        IFNULL(b.sort,0) &lt; #{cursorScore}
        OR (IFNULL(b.sort,0) = #{cursorScore} AND b.id &lt; #{cursorId})
      )
    </if>
    ORDER BY score DESC, b.id DESC
    LIMIT #{size}
    </script>
    """)
    List<BannerHomeVo> banners(@Param("cursorScore") Long cursorScore,
                               @Param("cursorId") Long cursorId,
                               @Param("size") Integer size);


    // ===================== 公告 notices（启用 + 可见 + 时间窗口） =====================
    @Select("""
    <script>
    SELECT
      n.id,
      n.title,
      n.cover_url AS coverUrl,
      n.publisher,
      n.create_time AS createTime,
      n.sort,
      IFNULL(n.sort,0) AS score
    FROM music_notice n
    WHERE n.status = 1
      AND n.visible = 1
      AND (n.start_time IS NULL OR n.start_time &lt;= NOW())
      AND (n.end_time IS NULL OR n.end_time &gt;= NOW())
    <if test="cursorScore != null and cursorId != null">
      AND (
        IFNULL(n.sort,0) &lt; #{cursorScore}
        OR (IFNULL(n.sort,0) = #{cursorScore} AND n.id &lt; #{cursorId})
      )
    </if>
    ORDER BY score DESC, n.id DESC
    LIMIT #{size}
    </script>
    """)
    List<NoticeHomeVo> notices(@Param("cursorScore") Long cursorScore,
                               @Param("cursorId") Long cursorId,
                               @Param("size") Integer size);
}
