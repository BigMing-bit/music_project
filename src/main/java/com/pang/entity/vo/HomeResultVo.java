package com.pang.entity.vo;

import lombok.Data;

@Data
public class HomeResultVo {
    private CursorPageResult<PlaylistHomeVo> playlists;
    private CursorPageResult<AlbumHomeVo> albums;
    private CursorPageResult<SongHomeVo> hotSongs;
    private CursorPageResult<BannerHomeVo> banners;
    private CursorPageResult<NoticeHomeVo> notices;
}
