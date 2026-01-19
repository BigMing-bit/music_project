package com.pang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.entity.PlayHistory;
import com.pang.entity.vo.CursorPageResult;
import com.pang.entity.vo.PlayHistoryVo;

public interface PlayHistoryService extends IService<PlayHistory> {

    // ✅ 播放歌曲：写入历史
    void addPlayHistory(Long userId, Long songId);

    // ✅ 查询播放历史（游标分页）
    CursorPageResult<PlayHistoryVo> getMyPlayHistory(Long userId, String cursor, Integer size);
}
