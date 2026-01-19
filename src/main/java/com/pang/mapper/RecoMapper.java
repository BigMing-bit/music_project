package com.pang.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface RecoMapper {

    /**
     * ✅ 计算歌曲共现（基于歌单）
     * 返回：song_id, sim_song_id, co_cnt
     */
    @Select("""
        SELECT a.song_id AS songId, b.song_id AS simSongId, COUNT(*) AS coCnt
        FROM music_playlist_song a
        JOIN music_playlist_song b
          ON a.playlist_id = b.playlist_id
         AND a.song_id <> b.song_id
        GROUP BY a.song_id, b.song_id
    """)
    List<Map<String, Object>> songCoOccurrenceFromPlaylists();

    /**
     * ✅ 每首歌出现在多少歌单里（用于余弦/归一化）
     */
    @Select("""
        SELECT song_id AS songId, COUNT(DISTINCT playlist_id) AS df
        FROM music_playlist_song
        GROUP BY song_id
    """)
    List<Map<String, Object>> songDocumentFrequency();


    /**
     * ✅ 歌单两两相似度（Jaccard）
     * score = intersection / union
     */
    @Select("""
        SELECT
          a.playlist_id AS playlistId,
          b.playlist_id AS simPlaylistId,
          (COUNT(DISTINCT a.song_id) / 
            (SELECT COUNT(DISTINCT x.song_id) FROM music_playlist_song x WHERE x.playlist_id = a.playlist_id)
           +
            (SELECT COUNT(DISTINCT y.song_id) FROM music_playlist_song y WHERE y.playlist_id = b.playlist_id)
           - COUNT(DISTINCT a.song_id)
          ) AS dummy
        FROM music_playlist_song a
        JOIN music_playlist_song b
          ON a.song_id = b.song_id
         AND a.playlist_id <> b.playlist_id
        GROUP BY a.playlist_id, b.playlist_id
    """)
    List<Map<String, Object>> playlistJaccardRaw();
}
