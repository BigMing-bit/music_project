package com.pang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pang.entity.Playlist;
import com.pang.entity.PlaylistFavorite;
import com.pang.entity.vo.CursorPageResult;
import com.pang.entity.vo.PlaylistVo;
import com.pang.mapper.PlaylistFavoriteMapper;
import com.pang.mapper.PlaylistMapper;
import com.pang.service.BaseFavoriteService;
import com.pang.service.PlaylistLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PlaylistLikeServiceImpl extends BaseFavoriteService<PlaylistFavorite, Playlist, PlaylistVo> implements PlaylistLikeService {

    private final PlaylistFavoriteMapper playlistFavoriteMapper;
    private final PlaylistMapper playlistMapper;

    @Override
    public boolean exists(Long userId, Long playlistId) {
        Long count = playlistMapper.countUserFavorite(userId, playlistId);
        return count > 0;
    }

    @Override
    protected void create(Long userId, Long playlistId) {
        playlistMapper.insertCollect(userId, playlistId);
    }

    @Override
    protected void delete(Long userId, Long playlistId) {
        playlistMapper.deleteCollect(userId, playlistId);
    }

    @Override
    protected void updateTargetCount(Long playlistId, int delta) {
        playlistMapper.updateCollectCount(playlistId, delta);
    }

    @Override
    public CursorPageResult<PlaylistVo> getMyLikedPlaylists(Long userId, String cursor, Integer size) {
        return getMyLikedItems(userId, cursor, size);
    }

    @Override
    public CursorPageResult<PlaylistVo> getMyLikedItems(Long userId, String cursor, Integer size) {
        // 构建查询条件
        LambdaQueryWrapper<PlaylistFavorite> qw = new LambdaQueryWrapper<>();
        qw.eq(PlaylistFavorite::getUserId, userId)
                .in(PlaylistFavorite::getStatus, 1, 3)
                .orderByDesc(PlaylistFavorite::getId);

        // 处理游标分页
        if (cursor != null && !cursor.isBlank()) {
            qw.lt(PlaylistFavorite::getId, Long.parseLong(cursor));
        }
        qw.last("LIMIT " + size);

        // 查询收藏记录并过滤无效数据
        List<PlaylistFavorite> likes = playlistFavoriteMapper.selectList(qw);
        List<Long> playlistIds = likes.stream()
                .map(PlaylistFavorite::getPlaylistId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 构建返回结果
        CursorPageResult<PlaylistVo> result = new CursorPageResult<>();
        result.setHasMore(likes.size() == size);

        // 无收藏记录时直接返回空列表
        if (playlistIds.isEmpty()) {
            result.setList(Collections.emptyList());
            result.setNextCursor(null);
            return result;
        }

        // 批量查询歌单信息
        List<Playlist> playlists = playlistMapper.selectList(
                new LambdaQueryWrapper<Playlist>()
                        .in(Playlist::getId, playlistIds)
                        .eq(Playlist::getStatus, 1)
        );

        // 按收藏顺序排序并转换为 VO
        Map<Long, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < playlistIds.size(); i++) {
            orderMap.put(playlistIds.get(i), i);
        }

        List<PlaylistVo> voList = playlists.stream()
                .sorted(Comparator.comparingInt(p -> orderMap.getOrDefault(p.getId(), Integer.MAX_VALUE)))
                .map(p -> {
                    PlaylistVo vo = new PlaylistVo();
                    vo.setId(p.getId());
                    vo.setName(p.getName());
                    vo.setCoverUrl(p.getCoverUrl());
                    vo.setPlayCount(p.getPlayCount());
                    vo.setCollectCount(p.getCollectCount());
                    return vo;
                })
                .collect(Collectors.toList());

        // 设置游标和返回结果
        result.setList(voList);
        result.setNextCursor(likes.isEmpty() ? null : likes.get(likes.size() - 1).getId().toString());
        return result;
    }

    @Override
    public List<PlaylistVo> getMyLikedItems(Long userId, Integer page, Integer pageSize) {
        // 实现传统分页（如果需要）
        return Collections.emptyList();
    }

}