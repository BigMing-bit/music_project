package com.pang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pang.entity.PlaylistSong;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface PlaylistSongMapper extends BaseMapper<PlaylistSong> {


    @Delete("DELETE FROM music_playlist_song WHERE playlist_id = #{playlistId}")
    void deleteByPlaylistId(@Param("playlistId") Long playlistId);

    @Insert({
            "<script>",
            "INSERT INTO music_playlist_song(playlist_id, song_id, sort) VALUES",
            "<foreach collection='songIds' item='sid' index='idx' separator=','>",
            "(#{playlistId}, #{sid}, #{idx})",
            "</foreach>",
            "</script>"
    })
    void insertBatch(@Param("playlistId") Long playlistId, @Param("songIds") List<Long> songIds);


    @Select("""
        SELECT song_id
        FROM music_playlist_song
        WHERE playlist_id = #{playlistId}
        ORDER BY sort ASC, create_time ASC
    """)
    List<Long> selectSongIds(@Param("playlistId") Long playlistId);


    @Delete({
            "<script>",
            "DELETE FROM music_playlist_song WHERE playlist_id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    void deleteByPlaylistIds(@Param("ids") List<Long> ids);


    @Select("""
        SELECT playlist_id AS playlistId, song_id AS songId
        FROM music_playlist_song
    """)
    List<Map<String, Object>> allPlaylistSongs();

}
