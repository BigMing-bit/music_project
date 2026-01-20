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

    // 常量定义
    private static final int ACTIVE_STATUS = 1;
    private static final int SPECIAL_STATUS = 3;

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
        List<PlaylistFavorite> likes = queryLikedPlaylists(userId, cursor, size);
        List<Long> playlistIds = extractPlaylistIds(likes);

        CursorPageResult<PlaylistVo> result = new CursorPageResult<>();
        result.setHasMore(likes.size() == size);

        if (playlistIds.isEmpty()) {
            result.setList(Collections.emptyList());
            result.setNextCursor(null);
            return result;
        }

        List<PlaylistVo> voList = buildPlaylistVoList(playlistIds);
        result.setList(voList);
        result.setNextCursor(getNextCursor(likes));
        return result;
    }

    /**
     * 查询用户收藏的歌单记录
     */
    private List<PlaylistFavorite> queryLikedPlaylists(Long userId, String cursor, Integer size) {
        LambdaQueryWrapper<PlaylistFavorite> qw = new LambdaQueryWrapper<>();
        qw.eq(PlaylistFavorite::getUserId, userId)
                .in(PlaylistFavorite::getStatus, ACTIVE_STATUS, SPECIAL_STATUS)
                .orderByDesc(PlaylistFavorite::getId);

        if (cursor != null && !cursor.isBlank()) {
            try {
                qw.lt(PlaylistFavorite::getId, Long.parseLong(cursor));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid cursor format: " + cursor);
            }
        }
        qw.last("LIMIT " + size);

        return playlistFavoriteMapper.selectList(qw);
    }

    /**
     * 提取歌单 ID 列表
     */
    private List<Long> extractPlaylistIds(List<PlaylistFavorite> likes) {
        return likes.stream()
                .map(PlaylistFavorite::getPlaylistId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 构建歌单 VO 列表
     */
    private List<PlaylistVo> buildPlaylistVoList(List<Long> playlistIds) {
        List<Playlist> playlists = playlistMapper.selectList(
                new LambdaQueryWrapper<Playlist>()
                        .in(Playlist::getId, playlistIds)
                        .eq(Playlist::getStatus, ACTIVE_STATUS)
        );

        Map<Long, Integer> orderMap = buildOrderMap(playlistIds);

        return playlists.stream()
                .sorted(Comparator.comparingInt(p -> orderMap.getOrDefault(p.getId(), Integer.MAX_VALUE)))
                .map(this::convertToPlaylistVo)
                .collect(Collectors.toList());
    }

    /**
     * 构建排序映射
     */
    private Map<Long, Integer> buildOrderMap(List<Long> playlistIds) {
        Map<Long, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < playlistIds.size(); i++) {
            orderMap.put(playlistIds.get(i), i);
        }
        return orderMap;
    }

    /**
     * 转换为 PlaylistVo 对象
     */
    private PlaylistVo convertToPlaylistVo(Playlist p) {
        return PlaylistVo.builder()
                .id(p.getId())
                .name(p.getName())
                .coverUrl(p.getCoverUrl())
                .playCount(p.getPlayCount())
                .collectCount(p.getCollectCount())
                .build();
    }

    /**
     * 获取下一页游标
     */
    private String getNextCursor(List<PlaylistFavorite> likes) {
        return likes.isEmpty() ? null : likes.get(likes.size() - 1).getId().toString();
    }
}
