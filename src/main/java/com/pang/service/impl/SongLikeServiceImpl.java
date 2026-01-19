package com.pang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pang.entity.Album;
import com.pang.entity.Singer;
import com.pang.entity.Song;
import com.pang.entity.SongLike;
import com.pang.entity.vo.CursorPageResult;
import com.pang.entity.vo.SongListVo;
import com.pang.mapper.AlbumMapper;
import com.pang.mapper.SingerMapper;
import com.pang.mapper.SongLikeMapper;
import com.pang.mapper.SongMapper;
import com.pang.service.BaseFavoriteService;
import com.pang.service.SongLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SongLikeServiceImpl extends BaseFavoriteService<SongLike, Song, SongListVo> implements SongLikeService {

    @Autowired private SongLikeMapper songLikeMapper;
    @Autowired private SongMapper songMapper;
    @Autowired private SingerMapper singerMapper;
    @Autowired private AlbumMapper albumMapper;

    @Override
    public boolean toggleLike(Long songId, Long userId) {
        return toggleFavorite(userId, songId);
    }

    @Override
    public boolean isLiked(Long songId, Long userId) {
        return exists(userId, songId);
    }

    @Override
    public boolean exists(Long userId, Long songId) {
        Integer count = Math.toIntExact(songLikeMapper.selectCount(
                new LambdaQueryWrapper<SongLike>()
                        .eq(SongLike::getUserId, userId)
                        .eq(SongLike::getSongId, songId)
                        .eq(SongLike::getStatus, 1)
        ));
        return count != null && count > 0;
    }

    @Override
    protected void create(Long userId, Long songId) {
        SongLike like = new SongLike();
        like.setUserId(userId);
        like.setSongId(songId);
        like.setStatus(1);
        songLikeMapper.insert(like);
    }

    @Override
    protected void delete(Long userId, Long songId) {
        songLikeMapper.delete(
                new LambdaQueryWrapper<SongLike>()
                        .eq(SongLike::getUserId, userId)
                        .eq(SongLike::getSongId, songId)
        );
    }

    @Override
    protected void updateTargetCount(Long songId, int delta) {
        if (delta > 0) {
            // 增加点赞数
            songMapper.update(null,
                    new LambdaUpdateWrapper<Song>()
                            .eq(Song::getId, songId)
                            .setSql("like_count = like_count + 1"));
        } else {
            // 减少点赞数（防止负数）
            songMapper.update(null,
                    new LambdaUpdateWrapper<Song>()
                            .eq(Song::getId, songId)
                            .setSql("like_count = IF(like_count>0, like_count-1, 0)"));
        }
    }

    @Override
    public CursorPageResult<SongListVo> getMyLikedSongs(Long userId, String cursor, Integer size) {
        return getMyLikedItems(userId, cursor, size);
    }

    @Override
    public CursorPageResult<SongListVo> getMyLikedItems(Long userId, String cursor, Integer size) {

        LambdaQueryWrapper<SongLike> qw = new LambdaQueryWrapper<>();
        qw.eq(SongLike::getUserId, userId)
                .eq(SongLike::getStatus, 1);

        // cursor 用 song_like.id
        if (cursor != null && !cursor.isBlank()) {
            qw.lt(SongLike::getId, Long.parseLong(cursor));
        }

        qw.orderByDesc(SongLike::getId)
                .last("LIMIT " + size);

        List<SongLike> likes = songLikeMapper.selectList(qw);
        List<Long> songIds = likes.stream().map(SongLike::getSongId).toList();

        CursorPageResult<SongListVo> result = new CursorPageResult<>();
        result.setHasMore(likes.size() == size);

        if (songIds.isEmpty()) {
            result.setList(Collections.emptyList());
            result.setNextCursor(null);
            return result;
        }

        // 批量查 song
        List<Song> songs = songMapper.selectList(
                new LambdaQueryWrapper<Song>()
                        .in(Song::getId, songIds)
                        .eq(Song::getStatus, 1)
        );

        // singerName 映射
        Map<Long, String> singerMap = getSingerMap(songs);
        
        // albumName 映射
        Map<Long, String> albumMap = getAlbumMap(songs);

        // 按 likes 顺序还原
        Map<Long, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < songIds.size(); i++) orderMap.put(songIds.get(i), i);
        songs.sort(Comparator.comparingInt(s -> orderMap.getOrDefault(s.getId(), 0)));

        List<SongListVo> voList = songs.stream().map(s ->
            SongListVo.builder()
                .id(s.getId())
                .songName(s.getSongName())
                .singerId(s.getSingerId())
                .singerName(singerMap.get(s.getSingerId()))
                .singerAvatarUrl(null) // 歌曲表中没有歌手头像URL，需要额外查询
                .albumId(s.getAlbumId())
                .albumName(albumMap.get(s.getAlbumId()))
                .coverUrl(s.getCoverUrl())
                .audioUrl(s.getAudioUrl())
                .playCount(s.getPlayCount())
                .likeCount(s.getLikeCount())
                .durationSeconds(s.getDurationSeconds())
                .build()
        ).toList();

        result.setList(voList);

        // nextCursor
        result.setNextCursor(likes.get(likes.size() - 1).getId().toString());
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

    private Map<Long, String> getAlbumMap(List<Song> songs) {
        List<Long> albumIds = songs.stream().map(Song::getAlbumId).filter(Objects::nonNull).distinct().toList();
        if (albumIds.isEmpty()) return new HashMap<>();

        List<Album> albums = albumMapper.selectList(
                new LambdaQueryWrapper<Album>().in(Album::getId, albumIds).eq(Album::getStatus, 1)
        );

        return albums.stream().collect(Collectors.toMap(Album::getId, Album::getAlbumName, (a, b) -> a));
    }
}
