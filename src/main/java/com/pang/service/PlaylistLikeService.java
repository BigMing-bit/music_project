package com.pang.service;

import com.pang.entity.vo.CursorPageResult;
import com.pang.entity.vo.PlaylistVo;

public interface PlaylistLikeService {

    CursorPageResult<PlaylistVo> getMyLikedPlaylists(Long userId, String cursor, Integer size);

}

