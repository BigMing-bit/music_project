package com.pang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pang.entity.Album;
import com.pang.entity.AlbumFavorite;
import com.pang.entity.vo.CursorPageResult;
import com.pang.entity.vo.AlbumVo;
import com.pang.mapper.AlbumFavoriteMapper;
import com.pang.mapper.AlbumMapper;
import com.pang.service.AlbumLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 专辑收藏服务实现类
 */
@Service
@RequiredArgsConstructor
public class AlbumLikeServiceImpl implements AlbumLikeService {

    private final AlbumFavoriteMapper albumFavoriteMapper;
    private final AlbumMapper albumMapper;

    @Override
    public boolean toggleFavorite(Long userId, Long albumId) {
        if (exists(userId, albumId)) {
            // 已收藏 -> 取消收藏
            albumMapper.deleteCollect(userId, albumId);
            albumMapper.updateCollectCount(albumId, -1);
            return false;
        } else {
            // 未收藏 -> 添加收藏
            albumMapper.insertCollect(userId, albumId);
            albumMapper.updateCollectCount(albumId, 1);
            return true;
        }
    }

    @Override
    public boolean exists(Long userId, Long albumId) {
        Long count = albumMapper.countUserFavorite(userId, albumId);
        return count != null && count > 0;
    }

    @Override
    public CursorPageResult<AlbumVo> getMyLikedAlbums(Long userId, String cursor, Integer size) {
        // 构建查询条件
        LambdaQueryWrapper<AlbumFavorite> qw = new LambdaQueryWrapper<>();
        qw.eq(AlbumFavorite::getUserId, userId)
                .in(AlbumFavorite::getStatus, 1, 3)
                .orderByDesc(AlbumFavorite::getId);

        // 处理游标分页
        if (cursor != null && !cursor.isBlank()) {
            qw.lt(AlbumFavorite::getId, Long.parseLong(cursor));  // 使用游标ID来分页
        }
        qw.last("LIMIT " + size);

        // 查询收藏记录并过滤无效数据
        List<AlbumFavorite> likes = albumFavoriteMapper.selectList(qw);
        List<Long> albumIds = likes.stream()
                .map(AlbumFavorite::getAlbumId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 构建返回结果
        CursorPageResult<AlbumVo> result = new CursorPageResult<>();
        result.setHasMore(likes.size() == size);

        // 无收藏记录时直接返回空列表
        if (albumIds.isEmpty()) {
            result.setList(Collections.emptyList());
            result.setNextCursor(null);
            return result;
        }

        // 批量查询专辑信息
        List<Album> albums = albumMapper.selectList(
                new LambdaQueryWrapper<Album>()
                        .in(Album::getId, albumIds)
                        .eq(Album::getStatus, 1)
        );

        // 按收藏顺序排序并转换为 VO
        Map<Long, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < albumIds.size(); i++) {
            orderMap.put(albumIds.get(i), i);
        }

        List<AlbumVo> voList = albums.stream()
                .sorted(Comparator.comparingInt(p -> orderMap.getOrDefault(p.getId(), Integer.MAX_VALUE)))
                .map(p -> {
                    AlbumVo vo = new AlbumVo();
                    vo.setId(p.getId());
                    vo.setAlbumName(p.getAlbumName());
                    vo.setSingerId(p.getSingerId());
                    vo.setCoverUrl(p.getCoverUrl());
                    vo.setPublishDate(p.getPublishDate());
                    vo.setCollectCount(p.getCollectCount());
                    return vo;
                })
                .collect(Collectors.toList());

        result.setList(voList);
        result.setNextCursor(likes.isEmpty() ? null : likes.get(likes.size() - 1).getId().toString());  // 使用游标的最后一项ID作为下一页的游标
        return result;
    }
}
