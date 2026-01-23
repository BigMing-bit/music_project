package com.pang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.PlayHistory;
import com.pang.entity.Singer;
import com.pang.entity.Song;
import com.pang.entity.vo.CursorPageResult;
import com.pang.entity.vo.PlayHistoryVo;
import com.pang.mapper.PlayHistoryMapper;
import com.pang.mapper.SingerMapper;
import com.pang.mapper.SongMapper;
import com.pang.service.PlayHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayHistoryServiceImpl extends ServiceImpl<PlayHistoryMapper, PlayHistory> implements PlayHistoryService {

    private final PlayHistoryMapper playHistoryMapper;
    private final SongMapper songMapper;
    private final SingerMapper singerMapper;

    @Override
    public void addPlayHistory(Long userId, Long songId) {

        // ✅ song.play_count +1
        songMapper.update(null,
                new LambdaUpdateWrapper<Song>()
                        .eq(Song::getId, songId)
                        .setSql("play_count = play_count + 1"));

        // ✅ 插入播放历史
        PlayHistory history = new PlayHistory();
        history.setUserId(userId);
        history.setSongId(songId);
        playHistoryMapper.insert(history);
    }

    @Override
    public CursorPageResult<PlayHistoryVo> getMyPlayHistory(Long userId, String cursor, Integer size) {

        LambdaQueryWrapper<PlayHistory> qw = new LambdaQueryWrapper<>();
        qw.eq(PlayHistory::getUserId, userId);

        if (cursor != null && !cursor.isBlank()) {
            qw.lt(PlayHistory::getId, Long.parseLong(cursor));
        }

        qw.orderByDesc(PlayHistory::getId)
                .last("LIMIT " + size);

        List<PlayHistory> list = playHistoryMapper.selectList(qw);
        List<Long> songIds = list.stream().map(PlayHistory::getSongId).toList();

        CursorPageResult<PlayHistoryVo> result = new CursorPageResult<>();
        result.setHasMore(list.size() == size);

        if (songIds.isEmpty()) {
            result.setList(Collections.emptyList());
            result.setNextCursor(null);
            return result;
        }

        List<Song> songs = songMapper.selectList(
                new LambdaQueryWrapper<Song>().in(Song::getId, songIds).eq(Song::getStatus, 1)
        );
        Map<Long, Song> songMap = songs.stream().collect(Collectors.toMap(Song::getId, s -> s));

        Map<Long, String> singerMap = getSingerMap(songs);

        List<PlayHistoryVo> voList = list.stream().map(h -> {
            Song s = songMap.get(h.getSongId());
            if (s == null) return null;

            PlayHistoryVo vo = new PlayHistoryVo();
            vo.setHistoryId(h.getId());
            vo.setSongId(s.getId());
            vo.setSongName(s.getSongName());
            vo.setSingerId(s.getSingerId());
            vo.setSingerName(singerMap.get(s.getSingerId()));
            vo.setCoverUrl(s.getCoverUrl());
            vo.setAudioUrl(s.getAudioUrl());
            vo.setDurationSeconds(s.getDurationSeconds());
            vo.setPlayTime(h.getPlayTime());
            return vo;
        }).filter(Objects::nonNull).toList();

        result.setList(voList);
        result.setNextCursor(list.get(list.size() - 1).getId().toString());
        return result;
    }

    private Map<Long, String> getSingerMap(List<Song> songs) {
        List<Long> singerIds = songs.stream().map(Song::getSingerId).filter(Objects::nonNull).distinct().toList();
        if (singerIds.isEmpty()) return new HashMap<>();

        List<Singer> singers = singerMapper.selectList(
                new LambdaQueryWrapper<Singer>().in(Singer::getId, singerIds).eq(Singer::getStatus, 1)
        );
        return singers.stream().collect(Collectors.toMap(Singer::getId, Singer::getName, (a, b) -> a));
    }
}
