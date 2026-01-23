package com.pang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.common.constants.BusinessConstants;
import com.pang.entity.Album;
import com.pang.entity.Query.AlbumQueryParam;
import com.pang.entity.Singer;
import com.pang.entity.Song;
import com.pang.entity.vo.*;
import com.pang.mapper.AlbumMapper;
import com.pang.mapper.SingerMapper;
import com.pang.mapper.SongMapper;
import com.pang.service.AlbumService;
import com.pang.service.AlbumLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl extends ServiceImpl<AlbumMapper, Album> implements AlbumService {

    private final AlbumLikeService albumLikeService;

    private final SingerMapper singerMapper;

    private final SongMapper songMapper;

    private final AlbumMapper albumMapper;

    @Override
     public AlbumDetailVo getAlbumDetail(Long albumId) {
       Album album = this.getOne(
               new LambdaQueryWrapper<Album>()
                       .eq(Album::getId, albumId)
                       .eq(Album::getStatus, BusinessConstants.ACTIVE_STATUS)
       );

       if (album == null) return null;

       Singer singer = singerMapper.selectOne(
               new LambdaQueryWrapper<Singer>()
                       .eq(Singer::getId, album.getSingerId())
                       .eq(Singer::getStatus, BusinessConstants.ACTIVE_STATUS)
       );

       return AlbumDetailVo.builder()
               .id(album.getId())
               .albumName(album.getAlbumName())
               .coverUrl(album.getCoverUrl())
               .singerId(album.getSingerId())
               .singerName(singer != null ? singer.getName() : null)
               .build();
   }

    @Override
    public CursorPageResult<SongListVo> getAlbumSongs(Long albumId, String cursor, Integer size) {
        List<Song> songs = queryAlbumSongs(albumId, cursor, size);

        CursorPageResult<SongListVo> result = new CursorPageResult<>();
        result.setHasMore(songs.size() == size);

        if (songs.isEmpty()) {
            result.setList(Collections.emptyList());
            result.setNextCursor(null);
            return result;
        }

        List<SongListVo> voList = buildSongVoList(songs);
        result.setList(voList);
        result.setNextCursor(getNextCursor(songs));
        return result;
    }
    private List<Song> queryAlbumSongs(Long albumId, String cursor, Integer size) {
        LambdaQueryWrapper<Song> qw = new LambdaQueryWrapper<>();
        qw.eq(Song::getAlbumId, albumId)
                .eq(Song::getStatus, 1);

        if (cursor != null && !cursor.isBlank()) {
            qw.lt(Song::getId, Long.parseLong(cursor));
        }

        qw.orderByDesc(Song::getId)
                .last("LIMIT " + size);

        return songMapper.selectList(qw);
    }

    private List<SongListVo> buildSongVoList(List<Song> songs) {
        Map<Long, String> singerMap = getSingerMap(songs);

        return songs.stream().map(s ->
                SongListVo.builder()
                        .id(s.getId())
                        .songName(s.getSongName())
                        .singerId(s.getSingerId())
                        .singerName(singerMap.get(s.getSingerId()))
                        .coverUrl(s.getCoverUrl())
                        .audioUrl(s.getAudioUrl())
                        .playCount(s.getPlayCount())
                        .likeCount(s.getLikeCount())
                        .durationSeconds(s.getDurationSeconds())
                        .build()
        ).toList();
    }
    private String getNextCursor(List<Song> songs) {
        return songs.isEmpty() ? null : songs.get(songs.size() - 1).getId().toString();
    }
    private Map<Long, String> getSingerMap(List<Song> songs) {
        List<Long> singerIds = extractDistinctSingerIds(songs);

        if (singerIds.isEmpty()) return Collections.emptyMap();

        List<Singer> singers = singerMapper.selectList(
                new LambdaQueryWrapper<Singer>()
                        .in(Singer::getId, singerIds)
                        .eq(Singer::getStatus, 1)
        );

        return singers.stream()
                .collect(Collectors.toMap(Singer::getId, Singer::getName, (a, b) -> a));
    }
    private List<Long> extractDistinctSingerIds(List<Song> songs) {
        return songs.stream()
                .map(Song::getSingerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    @Override
    public IPage<AlbumVo> adminPageVo(AlbumQueryParam albumQueryParam) {
        Page<Album> page = new Page<>(albumQueryParam.getPageNum(), albumQueryParam.getPageSize());

        return baseMapper.pageAlbumVo(page,
                                     albumQueryParam.getKeyword(),
                                     albumQueryParam.getStatus(),
                                     albumQueryParam.getSingerId());
    }

    @Override
    public List<OptionVo> adminSelectOptions(String keyword) {
        return baseMapper.selectOptions(keyword);
    }

    @Override
    public IPage<AlbumVo> getAlbumPage(String name, Long singerId, Integer page, Integer pageSize) {
        Integer validatedPage = validatePageParam(page, BusinessConstants.DEFAULT_PAGE_NUM);
        Integer validatedPageSize = validatePageParam(pageSize, BusinessConstants.DEFAULT_PAGE_SIZE);

        Page<Album> albumPage = new Page<>(validatedPage, validatedPageSize);
        return baseMapper.pageAlbumVo(albumPage, name, BusinessConstants.ACTIVE_STATUS, singerId);
    }


    @Override
    public boolean toggleFavorite(Long albumId, Long userId) {
        return albumLikeService.toggleFavorite(userId, albumId);
    }

    @Override
    public void updateSingerName(Long albumId) {
        // 查询专辑信息
        Album album = albumMapper.selectById(albumId);
        if (album != null && album.getSingerId() != null) {
            // 获取歌手信息
            Singer singer = singerMapper.selectById(album.getSingerId());
            if (singer != null) {
                // 更新专辑表中的 singerName 字段
                album.setSingerName(singer.getName());
                // 更新数据库中的专辑记录
                albumMapper.updateById(album);
            }
        }
    }
    private Integer validatePageParam(Integer param, Integer defaultValue) {
        return (param == null || param <= 0) ? defaultValue : param;
    }
}
