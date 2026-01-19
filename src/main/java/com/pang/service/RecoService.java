package com.pang.service;

import java.util.List;
import java.util.Map;

public interface RecoService {
    List<Long> recommendSongIds(Long userId, int size);
    List<Long> recommendPlaylistIds(Long userId, Long seedPlaylistId, int size);
    List<Long> recommendPlaylistIdsWithFilter(Long userId, Long seedPlaylistId, int size, Map<String, Object> filters);
}
