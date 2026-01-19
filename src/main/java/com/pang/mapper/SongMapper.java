package com.pang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pang.entity.Song;
import com.pang.entity.vo.OptionVo;
import com.pang.entity.vo.PlaylistSongItemVo;
import com.pang.entity.vo.SongSimpleVo;
import com.pang.entity.vo.SongVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface  SongMapper extends BaseMapper<Song> {

    // . 根据ID查询歌曲（完整信息，包含歌手和专辑）
    @Select("SELECT s.*, music_singer.name as singer_name, music_album.album_name as album_name " +
            "FROM music_song s " +
            "LEFT JOIN music_singer ON s.singer_id = music_singer.id " +
            "LEFT JOIN music_album ON s.album_id = music_album.id " +
            "WHERE s.id = #{id}")
    SongVo selectDetailById(@Param("id") Long id);


    // 10. 根据歌手ID查询歌曲列表
    @Select("SELECT s.*, music_singer.name as singer_name, music_album.album_name as album_name " +
            "FROM music_song s " +
            "LEFT JOIN music_singer ON s.singer_id = music_singer.id " +
            "LEFT JOIN music_album ON s.album_id = music_album.id " +
            "WHERE s.singer_id = #{singerId} AND s.status = 1 " +
            "ORDER BY s.create_time DESC")
    List<SongVo> selectBySingerId(@Param("singerId") Long singerId);


    // 12. 热门歌曲（按播放量排序）
    @Select("SELECT s.*, music_singer.name as singer_name, music_album.album_name as album_name " +
            "FROM music_song s " +
            "LEFT JOIN music_singer ON s.singer_id = music_singer.id " +
            "LEFT JOIN music_album ON s.album_id = music_album.id " +
            "WHERE s.status = 1 " +
            "ORDER BY s.play_count DESC " +
            "LIMIT #{limit}")
    List<SongVo> selectHotSongs(@Param("limit") Integer limit);

    // 13. 新歌推荐
    @Select("SELECT s.*, music_singer.name as singer_name, music_album.album_name as album_name " +
            "FROM music_song s " +
            "LEFT JOIN music_singer ON s.singer_id = music_singer.id " +
            "LEFT JOIN music_album ON s.album_id = music_album.id " +
            "WHERE s.status = 1 " +
            "ORDER BY s.create_time DESC " +
            "LIMIT #{limit}")
    List<SongVo> selectNewSongs(@Param("limit") Integer limit);


    // 6. 更新播放量
    @Update("UPDATE music_song SET play_count = play_count + 1 WHERE id = #{id}")
    int incrementPlayCount(@Param("id") Long id);


    @Select("""
        SELECT 
            s.*,
            si.name AS singer_name,
            al.album_name AS album_name
        FROM music_song s
        LEFT JOIN music_singer si ON s.singer_id = si.id
        LEFT JOIN music_album al ON s.album_id = al.id
        WHERE 1=1
          AND (#{keyword} IS NULL OR s.song_name LIKE CONCAT('%', #{keyword}, '%'))
          AND (#{status} IS NULL OR s.status = #{status})
          AND (#{singerId} IS NULL OR s.singer_id = #{singerId})
          AND (#{albumId} IS NULL OR s.album_id = #{albumId})
        ORDER BY
          CASE 
            WHEN #{orderBy} = 'hot' THEN s.play_count
            WHEN #{orderBy} = 'like' THEN s.like_count
            ELSE UNIX_TIMESTAMP(s.create_time)
          END DESC
    """)
    IPage<SongVo> selectSongVoPage(Page<SongVo> page,
                                   @Param("keyword") String keyword,
                                   @Param("status") Integer status,
                                   @Param("singerId") Long singerId,
                                   @Param("albumId") Long albumId,
                                   @Param("orderBy") String orderBy);



    @Select("""
      SELECT
        s.id AS value,
        CONCAT(s.song_name, ' - ', si.name, ' - ', al.album_name) AS label
      FROM music_song s
      LEFT JOIN music_singer si ON s.singer_id = si.id
      LEFT JOIN music_album  al ON s.album_id = al.id
      WHERE (#{keyword} IS NULL OR #{keyword} = ''
             OR s.song_name LIKE CONCAT('%', #{keyword}, '%')
             OR si.name LIKE CONCAT('%', #{keyword}, '%')
             OR al.album_name LIKE CONCAT('%', #{keyword}, '%'))
      ORDER BY s.id DESC
      LIMIT 50
    """)
    List<OptionVo> selectSongOptions(@Param("keyword") String keyword);


    @Select({
            "<script>",
            "SELECT s.id AS value,",
            "CONCAT(s.song_name,' - ',IFNULL(si.name,''),' - ',IFNULL(al.album_name,'')) AS label",
            "FROM music_song s",
            "LEFT JOIN music_singer si ON s.singer_id = si.id",
            "LEFT JOIN music_album al ON s.album_id = al.id",
            "WHERE s.id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<OptionVo> selectSongOptionsByIds(@Param("ids") List<Long> ids);


    @Select("""
        SELECT
            s.id            AS id,
            s.song_name     AS songName,
            si.name         AS singerName
        FROM music_playlist_song ps
        JOIN music_song s ON ps.song_id = s.id
        LEFT JOIN music_singer si ON s.singer_id = si.id
        WHERE ps.playlist_id = #{playlistId}
        ORDER BY ps.sort ASC, ps.id ASC
    """)
    List<PlaylistSongItemVo> selectSongsByPlaylistId(@Param("playlistId") Long playlistId);



}
