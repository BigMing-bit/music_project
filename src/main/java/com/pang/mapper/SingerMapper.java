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

    // 1. 歌手详情（包含歌曲数量、总播放量统计）
    @Select("SELECT s.*, " +
            "COUNT(ms.id) as song_count, " +
            "COALESCE(SUM(ms.play_count), 0) as total_play_count " +
            "FROM music_singer s " +
            "LEFT JOIN music_song ms ON s.id = ms.singer_id AND ms.status = 1 " +
            "WHERE s.id = #{id} " +
            "GROUP BY s.id")
    SingerVo selectSingerDetail(@Param("id") Long id);

    // 2. 热门歌手（按歌曲总播放量排序，用于首页）
    @Select("SELECT s.*, " +
            "COUNT(ms.id) as song_count, " +
            "COALESCE(SUM(ms.play_count), 0) as total_play_count " +
            "FROM music_singer s " +
            "LEFT JOIN music_song ms ON s.id = ms.singer_id AND ms.status = 1 " +
            "WHERE s.status = 1 " +
            "GROUP BY s.id " +
            "ORDER BY total_play_count DESC " +
            "LIMIT #{limit}")
    List<SingerVo> selectHotSingers(@Param("limit") Integer limit);

    // 3. 最新歌手（按创建时间排序）
    @Select("SELECT s.*, " +
            "COUNT(ms.id) as song_count, " +
            "COALESCE(SUM(ms.play_count), 0) as total_play_count " +
            "FROM music_singer s " +
            "LEFT JOIN music_song ms ON s.id = ms.singer_id AND ms.status = 1 " +
            "WHERE s.status = 1 " +
            "GROUP BY s.id " +
            "ORDER BY MAX(s.create_time) DESC " +
            "LIMIT #{limit}")
    List<SingerVo> selectNewSingers(@Param("limit") Integer limit);

    // 4. 搜索歌手（按歌手名模糊搜索）
    @Select("SELECT s.*, " +
            "COUNT(ms.id) as song_count, " +
            "COALESCE(SUM(ms.play_count), 0) as total_play_count " +
            "FROM music_singer s " +
            "LEFT JOIN music_song ms ON s.id = ms.singer_id AND ms.status = 1 " +
            "WHERE s.status = 1 AND s.name LIKE CONCAT('%', #{keyword}, '%') " +
            "GROUP BY s.id " +
            "ORDER BY MAX(s.create_time) DESC")
    List<SingerVo> searchSingers(@Param("keyword") String keyword);

    // 5. 相似歌手推荐（同性别歌手，按热度排序）
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

    // 6. 根据歌手ID查询歌曲（包含专辑信息）
    @Select("SELECT s.*, singer.name as singer_name, album.album_name as album_name " +
            "FROM music_song s " +
            "LEFT JOIN music_singer singer ON s.singer_id = singer.id " +
            "LEFT JOIN music_album album ON s.album_id = album.id " +
            "WHERE s.singer_id = #{singerId} AND s.status = 1 " +
            "ORDER BY s.create_time DESC")
    List<SongVo> selectSongsBySingerId(@Param("singerId") Long singerId);

    // 7. 分页查询歌手的歌曲（包含专辑信息）
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

    // 8. 统计歌手的歌曲数量
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