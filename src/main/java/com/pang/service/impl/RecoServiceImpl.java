package com.pang.service.impl;

import com.pang.entity.SongSim;
import com.pang.service.PlayHistoryService;
import com.pang.service.RecoService;
import com.pang.service.SongService;
import com.pang.service.SongSimService;
import com.pang.service.PlaylistService;
import com.pang.service.PlaylistSimService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecoServiceImpl implements RecoService {

    private final PlayHistoryService playHistoryService;
    private final SongSimService songSimService;
    private final SongService songService;

    private final PlaylistSimService playlistSimService;
    private final PlaylistService playlistService;

    public RecoServiceImpl(PlayHistoryService playHistoryService,
                           SongSimService songSimService,
                           SongService songService,
                           PlaylistSimService playlistSimService,
                           PlaylistService playlistService) {
        this.playHistoryService = playHistoryService;
        this.songSimService = songSimService;
        this.songService = songService;
        this.playlistSimService = playlistSimService;
        this.playlistService = playlistService;
    }

    @Override
    public List<Long> recommendSongIds(Long userId, int size) {

        // 1) 没 userId：直接热门兜底
        if (userId == null) {
            return songService.lambdaQuery()
                    .eq(com.pang.entity.Song::getStatus, 1)
                    .orderByDesc(com.pang.entity.Song::getPlayCount)
                    .last("LIMIT " + size)
                    .list()
                    .stream().map(com.pang.entity.Song::getId).toList();
        }

        // 2) 用户最近听的 N 首
        List<Long> recent = playHistoryService.lambdaQuery()
                .eq(com.pang.entity.PlayHistory::getUserId, userId)
                .orderByDesc(com.pang.entity.PlayHistory::getPlayTime)
                .last("LIMIT 30")
                .list()
                .stream().map(com.pang.entity.PlayHistory::getSongId)
                .distinct()
                .toList();

        if (recent.isEmpty()) {
            // 兜底：热门
            return songService.lambdaQuery()
                    .eq(com.pang.entity.Song::getStatus, 1)
                    .orderByDesc(com.pang.entity.Song::getPlayCount)
                    .last("LIMIT " + size)
                    .list()
                    .stream().map(com.pang.entity.Song::getId).toList();
        }

        // 3) 汇总相似歌曲打分
        Map<Long, Double> scoreMap = new HashMap<>();
        Set<Long> seen = new HashSet<>(recent);

        for (Long seed : recent) {
            List<SongSim> sims = songSimService.lambdaQuery()
                    .eq(SongSim::getSongId, seed)
                    .orderByDesc(SongSim::getScore)
                    .last("LIMIT 20")
                    .list();

            for (SongSim s : sims) {
                Long cand = s.getSimSongId();
                if (seen.contains(cand)) continue;
                scoreMap.merge(cand, s.getScore().doubleValue(), Double::sum);
            }
        }

        // 4) Top size
        return scoreMap.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(size)
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public List<Long> recommendPlaylistIds(Long userId, Long seedPlaylistId, int size) {

        // ✅ 优先：如果有 seedPlaylistId（用户正在看某个歌单） -> 推荐相似歌单
        if (seedPlaylistId != null) {
            return playlistSimService.lambdaQuery()
                    .eq(com.pang.entity.PlaylistSim::getPlaylistId, seedPlaylistId)
                    .orderByDesc(com.pang.entity.PlaylistSim::getScore)
                    .last("LIMIT " + size)
                    .list()
                    .stream().map(com.pang.entity.PlaylistSim::getSimPlaylistId)
                    .toList();
        }

        // ✅ 没 seed 且用户也没数据：热门歌单兜底
        return playlistService.lambdaQuery()
                .eq(com.pang.entity.Playlist::getStatus, 1)
                .orderByDesc(com.pang.entity.Playlist::getPlayCount)
                .orderByDesc(com.pang.entity.Playlist::getCollectCount)
                .last("LIMIT " + size)
                .list()
                .stream().map(com.pang.entity.Playlist::getId).toList();
    }

    @Override
    public List<Long> recommendPlaylistIdsWithFilter(Long userId, Long seedPlaylistId, int size, Map<String, Object> filters) {
        // 1. 获取基础推荐结果
        List<Long> baseRecommendations = recommendPlaylistIds(userId, seedPlaylistId, size * 3);
        
        // 2. 过滤已收藏的歌单
        if (userId != null) {
            List<Long> filtered = new ArrayList<>();
            for (Long playlistId : baseRecommendations) {
                if (!playlistService.isFavorited(playlistId, userId)) {
                    filtered.add(playlistId);
                }
            }
            baseRecommendations = filtered;
        }
        
        // 3. 基于用户播放历史的加权推荐
        if (userId != null) {
            // 获取用户最近播放的歌曲
            List<Long> recentSongIds = playHistoryService.lambdaQuery()
                    .eq(com.pang.entity.PlayHistory::getUserId, userId)
                    .orderByDesc(com.pang.entity.PlayHistory::getPlayTime)
                    .last("LIMIT 20")
                    .list()
                    .stream().map(com.pang.entity.PlayHistory::getSongId)
                    .distinct()
                    .toList();
            
            if (!recentSongIds.isEmpty()) {
                // 计算歌单与用户历史的匹配度
                Map<Long, Double> scoreMap = new HashMap<>();
                
                // 为每个推荐的歌单计算匹配分数
                for (Long playlistId : baseRecommendations) {
                    List<Long> playlistSongIds = playlistService.listSongsByPlaylistId(playlistId)
                            .stream().map(item -> item.getId())
                            .collect(java.util.stream.Collectors.toList());
                    
                    int matchCount = 0;
                    for (Long songId : playlistSongIds) {
                        if (recentSongIds.contains(songId)) {
                            matchCount++;
                        }
                    }
                    
                    double score = playlistSongIds.isEmpty() ? 0 : (double) matchCount / playlistSongIds.size();
                    scoreMap.put(playlistId, score);
                }
                
                // 按分数排序
                baseRecommendations.sort((a, b) -> {
                    double scoreA = scoreMap.getOrDefault(a, 0.0);
                    double scoreB = scoreMap.getOrDefault(b, 0.0);
                    return Double.compare(scoreB, scoreA);
                });
            }
        }
        
        // 4. 应用其他过滤参数
        if (filters != null) {
            // 这里可以根据需要添加更多过滤逻辑
            // 例如：按风格、语言、年代等过滤
        }
        
        // 5. 限制返回数量
        return baseRecommendations.stream().limit(size).toList();
    }
}
