package com.pang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.entity.Playlist;
import com.pang.entity.vo.*;
import com.pang.security.dto.PlaylistSaveDTO;

import java.util.List;

public interface PlaylistService extends IService<Playlist> {

    CursorPageResult<PlaylistHomeVo> getHomePlaylists(String cursor, Integer size);

    PlaylistDetailVo getPlaylistDetail(Long playlistId,Long userId);

    Page<SongListVo> getPlaylistSongs(Long playlistId, Integer page, Integer pageSize);

    CursorPageResult<SongListVo> getPlaylistSongsCursor(Long playlistId, String cursor, Integer size);

    CursorPageResult<PlaylistHomeVo> getOfficialPlaylists(String cursor, Integer size);

    IPage<PlaylistVo> pageVo(int pageNum, int pageSize, String keyword, Integer status);


    void increasePlayFavorite(Long playlistId);

    boolean toggleFavorite(Long playlistId, Long userId);

    boolean isFavorited(Long playlistId, Long userId);


    Long saveOrUpdateWithSongs(PlaylistSaveDTO dto);


    List<MyPlaylistVO> getMyPlaylists(Long userId);


    List<PlaylistSongItemVo> listSongsByPlaylistId(Long playlistId);

    List<PlaylistHomeVo> listByTags(List<Long> tagIds);

    void editPlaylist(Long playlistId, PlaylistSaveDTO dto, Long operatorUserId);
}
