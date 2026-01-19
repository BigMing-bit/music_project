package com.pang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pang.entity.Tag;
import com.pang.entity.vo.TagVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    @Select("""
        SELECT id, name, type, sort
        FROM music_tag
        WHERE status = 1
        ORDER BY type ASC, sort ASC, id ASC
    """)
    List<TagVo> listEnabled();

    @Select("""
    SELECT t.id, t.name, t.type
    FROM music_tag t
    JOIN music_playlist_tag pt ON pt.tag_id = t.id
    WHERE pt.playlist_id = #{playlistId} AND t.status = 1
    ORDER BY t.type ASC, t.sort ASC, t.id ASC
""")
    List<TagVo> listByPlaylistId(@Param("playlistId") Long playlistId);

}
