package com.pang.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pang.entity.PlaylistSim;
import com.pang.entity.SongSim;
import com.pang.mapper.PlaylistSongMapper;
import com.pang.mapper.RecoMapper;
import com.pang.service.PlaylistSimService;
import com.pang.service.RecoBuildService;
import com.pang.service.SongSimService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecoBuildServiceImpl implements RecoBuildService {

    private final RecoMapper recoMapper;
    private final PlaylistSongMapper playlistSongMapper;
    private final SongSimService songSimService;
    private final PlaylistSimService playlistSimService;

    public RecoBuildServiceImpl(RecoMapper recoMapper,
                                PlaylistSongMapper playlistSongMapper,
                                SongSimService songSimService,
                                PlaylistSimService playlistSimService) {
        this.recoMapper = recoMapper;
        this.playlistSongMapper = playlistSongMapper;
        this.songSimService = songSimService;
        this.playlistSimService = playlistSimService;
    }

    /**
     * ✅ 歌曲相似度：用歌单共现做 ItemCF
     * score = coCnt / sqrt(df(song) * df(simSong))
     */
    @Override
    @Transactional
    public void rebuildSongSimFromPlaylists(int topK) {

        // 1) 清空旧缓存
        songSimService.remove(Wrappers.query());

        // 2) df：每首歌出现的歌单数
        Map<Long, Integer> dfMap = recoMapper.songDocumentFrequency().stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("songId")).longValue(),
                        m -> ((Number) m.get("df")).intValue()
                ));

        // 3) 共现
        List<Map<String, Object>> rows = recoMapper.songCoOccurrenceFromPlaylists();

        // song -> list(sim, score)
        Map<Long, List<SongSim>> bucket = new HashMap<>();

        for (Map<String, Object> r : rows) {
            long songId = ((Number) r.get("songId")).longValue();
            long simSongId = ((Number) r.get("simSongId")).longValue();
            int coCnt = ((Number) r.get("coCnt")).intValue();

            int df1 = dfMap.getOrDefault(songId, 1);
            int df2 = dfMap.getOrDefault(simSongId, 1);

            double score = coCnt / Math.sqrt((double) df1 * df2);

            SongSim sim = new SongSim();
            sim.setSongId(songId);
            sim.setSimSongId(simSongId);
            sim.setScore(BigDecimal.valueOf(score));
            sim.setCreateTime(LocalDateTime.now());

            bucket.computeIfAbsent(songId, k -> new ArrayList<>()).add(sim);
        }

        // 4) 每首歌取 TopK 落库
        List<SongSim> saveList = new ArrayList<>();
        for (List<SongSim> sims : bucket.values()) {
            List<SongSim> top = sims.stream()
                    .sorted((a, b) -> b.getScore().compareTo(a.getScore()))
                    .limit(topK)
                    .toList();
            saveList.addAll(top);
        }

        if (!saveList.isEmpty()) {
            songSimService.saveBatch(saveList, 1000);
        }
    }

    /**
     * ✅ 歌单相似度：Jaccard
     * score = |A∩B| / |A∪B|
     */
    @Override
    @Transactional
    public void rebuildPlaylistSim(int topK) {

        playlistSimService.remove(Wrappers.query());

        // 1) 拉全量 playlist->songs
        List<Map<String, Object>> rel = playlistSongMapper.allPlaylistSongs();

        // playlist -> songs set
        Map<Long, Set<Long>> p2s = new HashMap<>();
        for (Map<String, Object> r : rel) {
            long pid = ((Number) r.get("playlistId")).longValue();
            long sid = ((Number) r.get("songId")).longValue();
            p2s.computeIfAbsent(pid, k -> new HashSet<>()).add(sid);
        }

        List<Long> pids = new ArrayList<>(p2s.keySet());
        List<PlaylistSim> saveList = new ArrayList<>();

        for (int i = 0; i < pids.size(); i++) {
            long a = pids.get(i);
            Set<Long> sa = p2s.get(a);
            if (sa == null || sa.isEmpty()) continue;

            List<PlaylistSim> sims = new ArrayList<>();

            for (int j = 0; j < pids.size(); j++) {
                if (i == j) continue;
                long b = pids.get(j);
                Set<Long> sb = p2s.get(b);
                if (sb == null || sb.isEmpty()) continue;

                // intersection
                int inter = 0;
                Set<Long> small = sa.size() <= sb.size() ? sa : sb;
                Set<Long> big = sa.size() <= sb.size() ? sb : sa;
                for (Long x : small) if (big.contains(x)) inter++;

                if (inter == 0) continue;

                int union = sa.size() + sb.size() - inter;
                double score = (double) inter / union;

                PlaylistSim ps = new PlaylistSim();
                ps.setPlaylistId(a);
                ps.setSimPlaylistId(b);
                ps.setScore(BigDecimal.valueOf(score));
                ps.setCreateTime(LocalDateTime.now());
                sims.add(ps);
            }

            sims.sort((x, y) -> y.getScore().compareTo(x.getScore()));
            saveList.addAll(sims.stream().limit(topK).toList());
        }

        if (!saveList.isEmpty()) {
            playlistSimService.saveBatch(saveList, 500);
        }
    }
}
