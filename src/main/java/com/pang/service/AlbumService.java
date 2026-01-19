package com.pang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.entity.Album;
import com.pang.entity.Query.AlbumQueryParam;
import com.pang.entity.vo.*;

import java.util.List;

public interface AlbumService extends IService<Album> {

    AlbumDetailVo getAlbumDetail(Long albumId);

    CursorPageResult<SongListVo> getAlbumSongs(Long albumId, String cursor, Integer size);


    IPage<AlbumVo> getAlbumPage(String name, Long singerId, Integer page, Integer pageSize);

    boolean toggleFavorite(Long albumId, Long userId);

    IPage<AlbumVo> adminPageVo(AlbumQueryParam albumQueryParam);

    List<OptionVo> adminSelectOptions(String keyword);

    void updateSingerName(Long albumId);
}
