package com.pang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pang.entity.Singer;
import com.pang.entity.vo.OptionVo;
import com.pang.entity.vo.SingerVo;
import com.pang.entity.vo.SongVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SingerMapper extends BaseMapper<Singer> {

    @Select("SELECT s.*, " +
            "COUNT(ms.id) as song_count, " +
            "COALESCE(SUM(ms.play_count), 0) as total_play_count " +
            "FROM music_singer s " +
            "LEFT JOIN music_song ms ON s.id = ms.singer_id AND ms.status = 1 " +
            "WHERE s.id = #{id} " +
            "GROUP BY s.id")
    SingerVo selectSingerDetail(@Param("id") Long id);


    @Select("SELECT s.*, " +
            "COUNT(ms.id) as song_count, " +
            "COALESCE(SUM(ms.play_count), 0) as total_play_count " +
            "FROM music_singer s " +
            "LEFT JOIN music_song ms ON s.id = ms.singer_id AND ms.status = 1 " +
            "WHERE s.status = 1 AND s.id != #{singerId} AND s.gender = #{gender} " +
            "GROUP BY s.id " +
            "ORDER BY total_play_count DESC " +
            "LIMIT #{limit}")
    List<SingerVo> selectSimilarSingers(@Param("singerId") Long singerId,
                                        @Param("gender") Integer gender,
                                        @Param("limit") Integer limit);

    @Select("SELECT s.*, singer.name as singer_name, album.album_name as album_name " +
            "FROM music_song s " +
            "LEFT JOIN music_singer singer ON s.singer_id = singer.id " +
            "LEFT JOIN music_album album ON s.album_id = album.id " +
            "WHERE s.singer_id = #{singerId} AND s.status = 1 " +
            "ORDER BY s.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<SongVo> selectSongsBySingerIdPage(@Param("singerId") Long singerId,
                                           @Param("offset") Integer offset,
                                           @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM music_song WHERE singer_id = #{singerId} AND status = 1")
    Long countSongsBySingerId(@Param("singerId") Long singerId);


    @Select({
            "<script>",
            "SELECT id AS value, name AS label",
            "FROM music_singer",
            "<where>",
            "  status = 1",
            "  <if test='keyword != null and keyword.trim() != \"\"'>",
            "    AND name LIKE CONCAT('%', #{keyword}, '%')",
            "  </if>",
            "</where>",
            "ORDER BY id DESC",
            "LIMIT 50",
            "</script>"
    })
    List<OptionVo> selectOptions(@Param("keyword") String keyword);

}