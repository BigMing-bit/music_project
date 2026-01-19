package com.pang.service;

import com.pang.entity.vo.CursorPageResult;
import com.pang.entity.vo.SongListVo;

public interface SongLikeService {

    // ✅ 收藏/取消收藏歌曲
    boolean toggleLike(Long songId, Long userId);

    // ✅ 是否已收藏
    boolean isLiked(Long songId, Long userId);

    // ✅ 我的收藏歌曲列表（游标分页）
    CursorPageResult<SongListVo> getMyLikedSongs(Long userId, String cursor, Integer size);
}
