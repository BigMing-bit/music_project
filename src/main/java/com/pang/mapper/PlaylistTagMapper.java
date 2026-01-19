package com.pang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pang.entity.PlaylistTag;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PlaylistTagMapper extends BaseMapper<PlaylistTag> {

    @Delete("DELETE FROM music_playlist_tag WHERE playlist_id = #{playlistId}")
    void deleteByPlaylistId(@Param("playlistId") Long playlistId);

    // 批量插入
    @Insert("""
        <script>
        INSERT INTO music_playlist_tag(playlist_id, tag_id, create_time)
        VALUES
        <foreach collection="tagIds" item="tid" separator=",">
          (#{playlistId}, #{tid}, NOW())
        </foreach>
        </script>
    """)
    void insertBatch(@Param("playlistId") Long playlistId,
                     @Param("tagIds") List<Long> tagIds);
}
