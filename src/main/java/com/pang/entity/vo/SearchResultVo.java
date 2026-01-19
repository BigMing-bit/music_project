package com.pang.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchResultVo {
    private List<SongListVo> songs;
    private List<AlbumHomeVo> albums;
    private List<SingerHomeVo> singers;
    private List<PlaylistHomeVo> playlists;
}
