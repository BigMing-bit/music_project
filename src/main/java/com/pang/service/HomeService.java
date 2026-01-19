package com.pang.service;

import com.pang.entity.vo.*;

public interface HomeService {
    HomeResultVo getHome();

    CursorPageResult<BannerHomeVo> getBanners(String cursor, Integer size);
    CursorPageResult<NoticeHomeVo> getNotices(String cursor, Integer size);

    CursorPageResult<PlaylistHomeVo> getHotPlaylists(String cursor, Integer size);
    CursorPageResult<AlbumHomeVo> getHotAlbums(String cursor, Integer size);
    CursorPageResult<SongHomeVo> getHotSongs(String cursor, Integer size);
}

