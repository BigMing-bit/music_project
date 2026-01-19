package com.pang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pang.entity.Album;
import com.pang.entity.Playlist;
import com.pang.entity.Singer;
import com.pang.entity.Song;
import com.pang.entity.vo.*;
import com.pang.mapper.AlbumMapper;
import com.pang.mapper.PlaylistMapper;
import com.pang.mapper.SingerMapper;
import com.pang.mapper.SongMapper;
import com.pang.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired private SongMapper songMapper;
    @Autowired private SingerMapper singerMapper;
    @Autowired private AlbumMapper albumMapper;
    @Autowired private PlaylistMapper playlistMapper;

    @Override
    public CursorPageResult<?> search(String keyword, String type, String cursor, Integer size) {

        if (type == null || type.isBlank()) type = "all";
        if (keyword == null) keyword = "";

        switch (type) {
            case "song":
                return searchSongs(keyword, cursor, size);
            case "album":
                return searchAlbums(keyword, cursor, size);
            case "singer":
                return searchSingers(keyword, cursor, size);
            case "playlist":
                return searchPlaylists(keyword, cursor, size);
            default:
                // all：一次返回四类（各返回 size 条，适合首页搜索）
                SearchResultVo vo = new SearchResultVo();
                vo.setSongs(searchSongs(keyword, null, size).getList());
                vo.setAlbums(searchAlbums(keyword, null, size).getList());
                vo.setSingers(searchSingers(keyword, null, size).getList());
                vo.setPlaylists(searchPlaylists(keyword, null, size).getList());

                CursorPageResult<SearchResultVo> result = new CursorPageResult<>();
                result.setList(List.of(vo));
                result.setHasMore(false);
                result.setNextCursor(null);
                return result;
        }
    }

    private CursorPageResult<SongListVo> searchSongs(String keyword, String cursor, Integer size) {
        LambdaQueryWrapper<Song> qw = new LambdaQueryWrapper<>();
        qw.eq(Song::getStatus, 1)
                .like(Song::getSongName, keyword);

        if (cursor != null && !cursor.isBlank()) qw.lt(Song::getId, Long.parseLong(cursor));

        qw.orderByDesc(Song::getId).last("LIMIT " + size);

        List<Song> songs = songMapper.selectList(qw);

        List<SongListVo> list = songs.stream().map(s ->
            SongListVo.builder()
                .id(s.getId())
                .songName(s.getSongName())
                .singerId(s.getSingerId())
                .durationSeconds(s.getDurationSeconds())
                .coverUrl(s.getCoverUrl())
                .audioUrl(s.getAudioUrl())
                .playCount(s.getPlayCount())
                .likeCount(s.getLikeCount())
                    .build()
        ).toList();

        CursorPageResult<SongListVo> result = new CursorPageResult<>();
        result.setList(list);
        result.setHasMore(songs.size() == size);
        result.setNextCursor(songs.isEmpty() ? null : songs.get(songs.size() - 1).getId().toString());
        return result;
    }

    private CursorPageResult<AlbumHomeVo> searchAlbums(String keyword, String cursor, Integer size) {
        LambdaQueryWrapper<Album> qw = new LambdaQueryWrapper<>();
        qw.eq(Album::getStatus, 1)
                .like(Album::getAlbumName, keyword);

        if (cursor != null && !cursor.isBlank()) qw.lt(Album::getId, Long.parseLong(cursor));

        qw.orderByDesc(Album::getId).last("LIMIT " + size);

        List<Album> albums = albumMapper.selectList(qw);

        List<AlbumHomeVo> list = albums.stream().map(a -> {
            AlbumHomeVo vo = new AlbumHomeVo();
            vo.setId(a.getId());
            vo.setAlbumName(a.getAlbumName());
            vo.setCoverUrl(a.getCoverUrl());
            return vo;
        }).toList();

        CursorPageResult<AlbumHomeVo> result = new CursorPageResult<>();
        result.setList(list);
        result.setHasMore(albums.size() == size);
        result.setNextCursor(albums.isEmpty() ? null : albums.get(albums.size() - 1).getId().toString());
        return result;
    }

    private CursorPageResult<SingerHomeVo> searchSingers(String keyword, String cursor, Integer size) {
        LambdaQueryWrapper<Singer> qw = new LambdaQueryWrapper<>();
        qw.eq(Singer::getStatus, 1)
                .like(Singer::getName, keyword);

        if (cursor != null && !cursor.isBlank()) qw.lt(Singer::getId, Long.parseLong(cursor));

        qw.orderByDesc(Singer::getId).last("LIMIT " + size);

        List<Singer> singers = singerMapper.selectList(qw);

        List<SingerHomeVo> list = singers.stream().map(s -> {
            SingerHomeVo vo = new SingerHomeVo();
            vo.setId(s.getId());
            vo.setName(s.getName());
            vo.setGender(s.getGender());
            vo.setAvatarUrl(s.getAvatarUrl());
            return vo;
        }).toList();

        CursorPageResult<SingerHomeVo> result = new CursorPageResult<>();
        result.setList(list);
        result.setHasMore(singers.size() == size);
        result.setNextCursor(singers.isEmpty() ? null : singers.get(singers.size() - 1).getId().toString());
        return result;
    }

    private CursorPageResult<PlaylistHomeVo> searchPlaylists(String keyword, String cursor, Integer size) {
        LambdaQueryWrapper<Playlist> qw = new LambdaQueryWrapper<>();
        qw.eq(Playlist::getStatus, 1)
                .like(Playlist::getName, keyword);

        if (cursor != null && !cursor.isBlank()) qw.lt(Playlist::getId, Long.parseLong(cursor));

        qw.orderByDesc(Playlist::getId).last("LIMIT " + size);

        List<Playlist> playlists = playlistMapper.selectList(qw);

        List<PlaylistHomeVo> list = playlists.stream().map(p ->
            PlaylistHomeVo.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .description(p.getDescription())
                    .coverUrl(p.getCoverUrl())
                    .playCount(p.getPlayCount())
                    .collectCount(p.getCollectCount())
                    .build()
        ).toList();

        CursorPageResult<PlaylistHomeVo> result = new CursorPageResult<>();
        result.setList(list);
        result.setHasMore(playlists.size() == size);
        result.setNextCursor(playlists.isEmpty() ? null : playlists.get(playlists.size() - 1).getId().toString());
        return result;
    }
}
