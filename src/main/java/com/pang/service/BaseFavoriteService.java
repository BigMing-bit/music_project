package com.pang.service;

import com.pang.entity.BaseFavorite;
import com.pang.entity.vo.CursorPageResult;



/**
 * 通用收藏服务基类
 * @param <F> 收藏实体类型
 * @param <T> 目标实体类型
 * @param <VO> 响应VO类型
 */
public abstract class BaseFavoriteService<F extends BaseFavorite, T, VO> {

    /**
     * 收藏/取消收藏
     * @param userId 用户ID
     * @param targetId 目标ID（歌曲ID/歌单ID等）
     * @return true=已收藏，false=未收藏
     */
    public boolean toggleFavorite(Long userId, Long targetId) {
        if (exists(userId, targetId)) {
            // 已收藏 -> 取消收藏
            delete(userId, targetId);
            updateTargetCount(targetId, -1);
            return false;
        } else {
            // 未收藏 -> 添加收藏
            create(userId, targetId);
            updateTargetCount(targetId, 1);
            return true;
        }
    }

    /**
     * 检查是否已收藏
     * @param userId 用户ID
     * @param targetId 目标ID
     * @return 是否已收藏
     */
    public abstract boolean exists(Long userId, Long targetId);

    /**
     * 创建收藏记录
     * @param userId 用户ID
     * @param targetId 目标ID
     */
    protected abstract void create(Long userId, Long targetId);

    /**
     * 删除收藏记录
     * @param userId 用户ID
     * @param targetId 目标ID
     */
    protected abstract void delete(Long userId, Long targetId);

    /**
     * 更新目标的收藏/点赞计数
     * @param targetId 目标ID
     * @param delta 变化量（+1或-1）
     */
    protected abstract void updateTargetCount(Long targetId, int delta);

    /**
     * 获取我的收藏列表（游标分页）
     * @param userId 用户ID
     * @param cursor 游标
     * @param size 每页大小
     * @return 收藏列表
     */
    public abstract CursorPageResult<VO> getMyLikedItems(Long userId, String cursor, Integer size);

}
