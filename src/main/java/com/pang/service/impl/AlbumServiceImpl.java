package com.pang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AlbumServiceImpl extends ServiceImpl<AlbumMapper, Album> implements AlbumService {

    @Autowired
    private AlbumLikeService albumLikeService;

    @Autowired
    private SingerMapper singerMapper;

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private AlbumMapper albumMapper;

       /**
    * 获取专辑详情信息（包含关联的歌手名称）
    * 查询指定ID的专辑信息，并同时获取对应的歌手名称
    *
    * @param albumId 专辑ID，用于查询特定专辑的详细信息
    * @return AlbumDetailVo 包含专辑基本信息和歌手名称的视图对象，如果专辑不存在或状态无效则返回null
    */

    @Override
    public AlbumDetailVo getAlbumDetail(Long albumId) {
    // 根据专辑ID和有效状态查询专辑信息
    Album album = this.getOne(
            new LambdaQueryWrapper<Album>()
                    .eq(Album::getId, albumId)
                    .eq(Album::getStatus, 1)
    );

    if (album == null) return null;

    // 根据专辑关联的歌手ID和有效状态查询歌手信息
    Singer singer = singerMapper.selectOne(
            new LambdaQueryWrapper<Singer>()
                    .eq(Singer::getId, album.getSingerId())
                    .eq(Singer::getStatus, 1)
    );

    AlbumDetailVo vo = new AlbumDetailVo();
    vo.setId(album.getId());
    vo.setAlbumName(album.getAlbumName());
    vo.setCoverUrl(album.getCoverUrl());
    vo.setSingerId(album.getSingerId());
    vo.setSingerName(singer != null ? singer.getName() : null);

    return vo;
}


    // ✅ 专辑歌曲 Cursor（按 song.id 倒序）
    @Override
    public CursorPageResult<SongListVo> getAlbumSongs(Long albumId, String cursor, Integer size) {

        LambdaQueryWrapper<Song> qw = new LambdaQueryWrapper<>();
        qw.eq(Song::getAlbumId, albumId)
                .eq(Song::getStatus, 1);

        // cursor 用 song.id
        if (cursor != null && !cursor.isBlank()) {
            qw.lt(Song::getId, Long.parseLong(cursor));
        }

        qw.orderByDesc(Song::getId)
                .last("LIMIT " + size);

        List<Song> songs = songMapper.selectList(qw);

        CursorPageResult<SongListVo> result = new CursorPageResult<>();
        result.setHasMore(songs.size() == size);

        if (songs.isEmpty()) {
            result.setList(Collections.emptyList());
            result.setNextCursor(null);
            return result;
        }

        // ✅ singerName 批量查（虽然一个专辑一般只有一个歌手，但这里也支持合辑）
        Map<Long, String> singerMap = getSingerMap(songs);

        List<SongListVo> voList = songs.stream().map(s ->
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

        result.setList(voList);
        result.setNextCursor(songs.get(songs.size() - 1).getId().toString());
        return result;
    }

        /**
     * 根据歌曲列表获取歌手信息映射
     * 从歌曲列表中提取歌手ID，查询有效的歌手信息，并构建ID到姓名的映射关系
     * @param songs 歌曲列表，用于提取歌手ID
     * @return Map<Long, String> 歌手ID到歌手姓名的映射表，key为歌手ID，value为歌手姓名
     */
    private Map<Long, String> getSingerMap(List<Song> songs) {
        // 提取歌曲列表中的歌手ID，过滤空值并去重
        List<Long> singerIds = songs.stream()
                .map(Song::getSingerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (singerIds.isEmpty()) return new HashMap<>();

        // 查询状态为1的有效歌手信息
        List<Singer> singers = singerMapper.selectList(
                new LambdaQueryWrapper<Singer>()
                        .in(Singer::getId, singerIds)
                        .eq(Singer::getStatus, 1)
        );

        Map<Long, String> map = new HashMap<>();
        for (Singer s : singers) {
            map.put(s.getId(), s.getName());
        }
        return map;
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
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 20;

        Page<Album> albumPage = new Page<>(page, pageSize);
        return baseMapper.pageAlbumVo(albumPage, name, 1, singerId);
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
}
