package com.pang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pang.entity.Playlist;
import com.pang.entity.vo.PlaylistDetailVo;
import com.pang.entity.vo.PlaylistVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PlaylistMapper extends BaseMapper<Playlist> {

    @Select("""
        SELECT
            p.id,
            p.name,
            p.description,
            p.cover_url AS coverUrl,
            p.status,
            p.collect_count AS collectCount,
            p.play_count AS playCount,
            p.create_time AS createTime,
            p.creator_user_id AS creatorUserId,

            -- 主歌手（出现次数最多）
            (
              SELECT si.name
              FROM music_playlist_song  ps
              JOIN music_song so ON ps.song_id = so.id
              JOIN music_singer si ON so.singer_id = si.id
              WHERE ps.playlist_id = p.id
              GROUP BY si.id
              ORDER BY COUNT(*) DESC
              LIMIT 1
            ) AS singerName

        FROM music_playlist  p
        WHERE (#{keyword} IS NULL OR p.name LIKE CONCAT('%', #{keyword}, '%'))
          AND (#{status} IS NULL OR p.status = #{status})
        ORDER BY p.id DESC
        """)
    IPage<PlaylistVo> pageVo(
            IPage<PlaylistVo> page,
            @Param("keyword") String keyword,
            @Param("status") Integer status
    );

    @Select("""
    SELECT
      p.id,
      p.name,
      p.description,
      p.cover_url AS coverUrl,

      p.creator_user_id AS creatorUserId,
      COALESCE(u.nickname, u.username) AS creatorNickName,
      u.role AS creatorRole,

      p.play_count AS playCount,
      p.collect_count AS collectCount,
      p.create_time AS createTime,
      (SELECT COUNT(1) FROM music_playlist_song ps WHERE ps.playlist_id = p.id) AS songCount
    FROM music_playlist p
    LEFT JOIN app_user u ON u.id = p.creator_user_id
    WHERE p.id = #{id} AND p.status = 1
""")
    PlaylistDetailVo selectDetail(@Param("id") Long id);




    // 登录后：查是否收藏
    @Select("""
            SELECT COUNT(1)
            FROM music_favorite_playlist 
            WHERE user_id = #{userId} AND playlist_id = #{playlistId} AND status = 1
    """)
    Long countUserFavorite(@Param("userId") Long userId, @Param("playlistId") Long playlistId);



    // 收藏
    @Insert("""
    INSERT INTO music_favorite_playlist(user_id, playlist_id, status, create_time)
    VALUES(#{userId}, #{playlistId}, 1, NOW())
    ON DUPLICATE KEY UPDATE status = 1, create_time = NOW()
    """)
    void insertCollect(@Param("userId") Long userId, @Param("playlistId") Long playlistId);

    // 取消收藏
    @Delete("""
    DELETE FROM music_favorite_playlist
    WHERE user_id = #{userId} AND playlist_id = #{playlistId}
    """)
    void deleteCollect(@Param("userId") Long userId, @Param("playlistId") Long playlistId);

    // 歌单 collect_count +1 / -1（防止出现负数）
    @Update("""
    UPDATE music_playlist
    SET collect_count = CASE
        WHEN #{delta} < 0 AND collect_count <= 0 THEN 0
        ELSE collect_count + #{delta}
    END
    WHERE id = #{playlistId}
    """)
    void updateCollectCount(@Param("playlistId") Long playlistId, @Param("delta") int delta);


    @Select("""
    <script>
    SELECT p.*
    FROM music_playlist p
    JOIN music_playlist_tag pt ON pt.playlist_id = p.id
    WHERE p.status = 1
      AND pt.tag_id IN
      <foreach collection="tagIds" item="tid" open="(" close=")" separator=",">
        #{tid}
      </foreach>
    GROUP BY p.id
    HAVING COUNT(DISTINCT pt.tag_id) = #{cnt}
    ORDER BY p.id DESC
    </script>
""")
    List<Playlist> listByTagsAnd(@Param("tagIds") List<Long> tagIds,
                                 @Param("cnt") Integer cnt);


}
