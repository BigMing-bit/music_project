package com.pang.service;

import com.pang.entity.vo.CursorPageResult;
import com.pang.entity.vo.AlbumVo;

/**
 * 专辑收藏服务接口
 */
public interface AlbumLikeService {

    boolean toggleFavorite(Long userId, Long albumId);

    boolean exists(Long userId, Long albumId);

    CursorPageResult<AlbumVo> getMyLikedAlbums(Long userId, String cursor, Integer size);

}
