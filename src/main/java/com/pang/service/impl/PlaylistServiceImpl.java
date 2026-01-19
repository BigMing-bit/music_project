package com.pang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.*;
import com.pang.entity.vo.*;
import com.pang.mapper.*;
import com.pang.security.dto.PlaylistSaveDTO;
import com.pang.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistServiceImpl extends ServiceImpl<PlaylistMapper, Playlist> implements PlaylistService {

    private final PlaylistMapper playlistMapper;

    private final PlaylistSongMapper playlistSongMapper;

    private final SongMapper songMapper;

    private final SingerMapper singerMapper;

    private final UserMapper userMapper;

    private final SysAdminMapper sysAdminMapper;

    private final PlaylistTagMapper playlistTagMapper;
    private final TagMapper tagMapper;



    @Override
    public CursorPageResult<PlaylistHomeVo> getHomePlaylists(String cursor, Integer size) {

        LambdaQueryWrapper<Playlist> qw = new LambdaQueryWrapper<>();
        qw.eq(Playlist::getStatus, 1);

        // cursor：使用 createTime 做游标（简单稳定）
        if (cursor != null && !cursor.isBlank()) {
            // 验证是否为有效的日期时间格式
            if (isValidDateTimeFormat(cursor)) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    LocalDateTime dateTime = LocalDateTime.parse(cursor, formatter);
                    qw.lt(Playlist::getCreateTime, dateTime);
                } catch (DateTimeParseException e) {
                    log.error("Failed to parse date string: {}, error: {}", cursor, e.getMessage());
                }
            } else {
                log.warn("Invalid cursor format received: {}", cursor);
            }
        }

        // 最热排序：收藏数 desc
        qw.orderByDesc(Playlist::getCollectCount)
                .orderByDesc(Playlist::getCreateTime)
                .last("LIMIT " + size);

        List<Playlist> playlists = playlistMapper.selectList(qw);

        List<PlaylistHomeVo> list = playlists.stream().map(p ->
            PlaylistHomeVo.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .coverUrl(p.getCoverUrl())
                    .collectCount(p.getCollectCount())
                    .build()
        ).toList();

        CursorPageResult<PlaylistHomeVo> result = new CursorPageResult<>();
        result.setList(list);
        result.setHasMore(playlists.size() == size);

        if (!playlists.isEmpty()) {
            result.setNextCursor(playlists.get(playlists.size() - 1).getCreateTime().toString());
        }

        return result;
    }

    @Override
    public CursorPageResult<PlaylistHomeVo> getOfficialPlaylists(String cursor, Integer size) {

        LambdaQueryWrapper<Playlist> qw = new LambdaQueryWrapper<>();
        qw.eq(Playlist::getStatus, 1);

        // cursor：使用 createTime 做游标（简单稳定）
        if (cursor != null && !cursor.isBlank()) {
            // 验证是否为有效的日期时间格式
            if (isValidDateTimeFormat(cursor)) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    LocalDateTime dateTime = LocalDateTime.parse(cursor, formatter);
                    qw.lt(Playlist::getCreateTime, dateTime);
                } catch (DateTimeParseException e) {
                    log.error("Failed to parse date string: {}, error: {}", cursor, e.getMessage());
                }
            } else {
                log.warn("Invalid cursor format received: {}", cursor);
            }
        }

        // 最热排序：收藏数 desc
        qw.orderByDesc(Playlist::getCollectCount)
                .orderByDesc(Playlist::getCreateTime)
                .last("LIMIT " + size);

        List<Playlist> playlists = playlistMapper.selectList(qw);

        List<PlaylistHomeVo> list = playlists.stream().map(p ->
            PlaylistHomeVo.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .coverUrl(p.getCoverUrl())
                    .collectCount(p.getCollectCount()) // ✅ 用 collectCount 作为播放量/热度展示
                    .build()
        ).toList();

        CursorPageResult<PlaylistHomeVo> result = new CursorPageResult<>();
        result.setList(list);
        result.setHasMore(playlists.size() == size);

        if (!playlists.isEmpty()) {
            result.setNextCursor(playlists.get(playlists.size() - 1).getCreateTime().toString());
        }

        return result;
    }

    // ✅ 歌单详情头部信息

    @Override
    public PlaylistDetailVo getPlaylistDetail(Long playlistId, Long userId) {
        PlaylistDetailVo vo = playlistMapper.selectDetail(playlistId);
        if (vo == null) return null;

        // 是否收藏
        vo.setCollected(userId != null && playlistMapper.countUserFavorite(userId, playlistId) > 0);

        // tags
        vo.setTags(tagMapper.listByPlaylistId(playlistId));

        // 是否官方：交给前端判断 creatorRole==1
        return vo;
    }


    // ✅ 歌单里的歌曲列表（点击歌单进去才查）
    @Override
    public Page<SongListVo> getPlaylistSongs(Long playlistId, Integer page, Integer pageSize) {

        // 1) 先查 playlist_song 分页，拿到 songId 顺序
        Page<PlaylistSong> psPage = playlistSongMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<PlaylistSong>()
                        .eq(PlaylistSong::getPlaylistId, playlistId)
                        .orderByAsc(PlaylistSong::getSort)
                        .orderByAsc(PlaylistSong::getId)
        );

        List<Long> songIds = psPage.getRecords().stream()
                .map(PlaylistSong::getSongId)
                .toList();

        Page<SongListVo> result = new Page<>();
        result.setCurrent(psPage.getCurrent());
        result.setSize(psPage.getSize());
        result.setTotal(psPage.getTotal());

        if (songIds.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        // 2) 批量查 songs
        List<Song> songs = songMapper.selectList(
                new LambdaQueryWrapper<Song>()
                        .in(Song::getId, songIds)
                        .eq(Song::getStatus, 1)
        );

        // 3) singerId -> singerName 映射
        Map<Long, String> singerMap = getSingerNameMap(
                songs.stream().map(Song::getSingerId).filter(Objects::nonNull).distinct().toList()
        );

        // 4) 把 songs 按 songIds 顺序排回去（重要：保证歌单顺序）
        Map<Long, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < songIds.size(); i++) {
            orderMap.put(songIds.get(i), i);
        }
        songs.sort(Comparator.comparingInt(s -> orderMap.getOrDefault(s.getId(), Integer.MAX_VALUE)));

        // 5) 转 VO
        List<SongListVo> voList = songs.stream().map(s ->
            SongListVo.builder()
                    .id(s.getId())
                    .songName(s.getSongName())
                    .singerName(singerMap.getOrDefault(s.getSingerId(), "未知歌手"))
                    .coverUrl(s.getCoverUrl())
                    .audioUrl(s.getAudioUrl())
                    .playCount(s.getPlayCount())
                    .likeCount(s.getLikeCount())
                    .durationSeconds(s.getDurationSeconds())
                    .build()
        ).toList();
        result.setRecords(voList);
        return result;
    }

    private Map<Long, String> getSingerNameMap(List<Long> singerIds) {
        if (singerIds == null || singerIds.isEmpty()) return new HashMap<>();

        List<Singer> singers = singerMapper.selectList(
                new LambdaQueryWrapper<Singer>()
                        .in(Singer::getId, singerIds)
                        .eq(Singer::getStatus, 1)
        );

        return singers.stream()
                .collect(Collectors.toMap(Singer::getId, Singer::getName, (a, b) -> a));
    }


    @Override
    public CursorPageResult<SongListVo> getPlaylistSongsCursor(Long playlistId, String cursor, Integer size) {

        LambdaQueryWrapper<PlaylistSong> qw = new LambdaQueryWrapper<>();
        qw.eq(PlaylistSong::getPlaylistId, playlistId);

        // cursor 格式：sort_id 例如： 10_12345
        if (cursor != null && !cursor.isBlank()) {
            String[] arr = cursor.split("_");
            Integer cursorSort = Integer.parseInt(arr[0]);
            Long cursorId = Long.parseLong(arr[1]);

            qw.and(w -> w.gt(PlaylistSong::getSort, cursorSort)
                    .or()
                    .eq(PlaylistSong::getSort, cursorSort)
                    .gt(PlaylistSong::getId, cursorId));
        }

        qw.orderByAsc(PlaylistSong::getSort)
                .orderByAsc(PlaylistSong::getId)
                .last("LIMIT " + size);

        List<PlaylistSong> psList = playlistSongMapper.selectList(qw);

        // 取出 songIds
        List<Long> songIds = psList.stream().map(PlaylistSong::getSongId).toList();

        CursorPageResult<SongListVo> result = new CursorPageResult<>();
        result.setHasMore(psList.size() == size);

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
        Map<Long, String> singerMap = getSingerNameMap(
                songs.stream().map(Song::getSingerId).filter(Objects::nonNull).distinct().toList()
        );

        // 按 psList 的 songId 顺序排序
        Map<Long, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < songIds.size(); i++) {
            orderMap.put(songIds.get(i), i);
        }
        songs.sort(Comparator.comparingInt(s -> orderMap.getOrDefault(s.getId(), Integer.MAX_VALUE)));

        // 转 VO
        List<SongListVo> voList = songs.stream().map(s ->
            SongListVo.builder()
                .id(s.getId())
                .songName(s.getSongName())
                .singerName(singerMap.getOrDefault(s.getSingerId(), "未知歌手"))
                .coverUrl(s.getCoverUrl())
                .audioUrl(s.getAudioUrl())
                .playCount(s.getPlayCount())
                .likeCount(s.getLikeCount())
                .durationSeconds(s.getDurationSeconds())
                .build()
        ).toList();

        result.setList(voList);

        // nextCursor = 最后一条 playlist_song 的 sort + id
        PlaylistSong last = psList.get(psList.size() - 1);
        result.setNextCursor(last.getSort() + "_" + last.getId());

        return result;
    }

    @Override
    public void increasePlayFavorite(Long playlistId) {
        playlistMapper.update(null,
                new LambdaUpdateWrapper<Playlist>()
                        .eq(Playlist::getId, playlistId)
                        .setSql("play_count = play_count + 1"));
    }


    @Override
    @Transactional
    public boolean toggleFavorite(Long playlistId, Long userId) {
        if (playlistId == null || userId == null) throw new RuntimeException("参数不能为空");

        Long collected = baseMapper.countUserFavorite(userId, playlistId);
        if (collected > 0) {
            baseMapper.deleteCollect(userId, playlistId);
            baseMapper.updateCollectCount(playlistId, -1);
            return false;
        } else {
            baseMapper.insertCollect(userId, playlistId);
            baseMapper.updateCollectCount(playlistId, +1);
            return true;
        }
    }


    @Override
    public boolean isFavorited(Long playlistId, Long userId) {
        if (userId == null) return false;
        return baseMapper.countUserFavorite(userId, playlistId) > 0;
    }

    @Override
    public IPage<PlaylistVo> pageVo(int pageNum, int pageSize, String keyword, Integer status) {
        Page<PlaylistVo> page = new Page<>(pageNum, pageSize);
        return this.baseMapper.pageVo(page, keyword, status);
    }

    @Override
    @Transactional
    public Long saveOrUpdateWithSongs(PlaylistSaveDTO dto) {

        Playlist p;

        if (dto.getId() != null) {
            Playlist db = this.getById(dto.getId());
            if (db == null) throw new RuntimeException("歌单不存在");

            p = new Playlist();
            p.setId(db.getId());

            // ✅ 创建者永远不变
            p.setCreatorUserId(db.getCreatorUserId());

        } else {
            p = new Playlist();
            if (dto.getCreatorUserId() == null) throw new RuntimeException("creatorUserId不能为空");
            p.setCreatorUserId(dto.getCreatorUserId());
        }

        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setCoverUrl(dto.getCoverUrl());
        p.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        p.setUpdateTime(LocalDateTime.now());


        this.saveOrUpdate(p);

        Long playlistId = p.getId();

        playlistSongMapper.deleteByPlaylistId(playlistId);
        if (dto.getSongIds() != null && !dto.getSongIds().isEmpty()) {
            playlistSongMapper.insertBatch(playlistId, dto.getSongIds());
        }


        // 1) dto.tagIds == null：表示前端没传，保持原标签不变（不要删）
        // 2) dto.tagIds != null：表示前端明确要更新（可为空数组，代表清空）
        if (dto.getTagIds() != null) {
            playlistTagMapper.deleteByPlaylistId(playlistId);
            if (!dto.getTagIds().isEmpty()) {
                playlistTagMapper.insertBatch(playlistId, dto.getTagIds());
            }
        }

        return playlistId;
    }

    @Override
    @Transactional
    public void editPlaylist(Long playlistId, PlaylistSaveDTO dto, Long operatorUserId) {

        Playlist db = this.getById(playlistId);
        if (db == null) throw new RuntimeException("歌单不存在");

        User me = userMapper.selectById(operatorUserId);
        if (me == null) throw new RuntimeException("用户不存在");

        Integer myRole = me.getRole() == null ? 0 : me.getRole();

        // 歌单创建者
        Long creatorUserId = db.getCreatorUserId();
        User creator = creatorUserId == null ? null : userMapper.selectById(creatorUserId);
        Integer creatorRole = (creator == null || creator.getRole() == null) ? 0 : creator.getRole();

        // ✅ 权限判断
        if (myRole == 0) {
            // 普通用户：只能改自己的
            if (!operatorUserId.equals(creatorUserId)) {
                throw new RuntimeException("无权限编辑此歌单");
            }
        } else {
            // 官方：只能改官方创建的
            if (creatorRole != 1) {
                throw new RuntimeException("官方账号不能编辑普通用户歌单");
            }
        }

        // ✅ 强制归属（防前端篡改）
        dto.setId(playlistId);
        dto.setCreatorUserId(creatorUserId);

        // ✅ 最终保存
        this.saveOrUpdateWithSongs(dto);
    }

    @Override
    public List<PlaylistSongItemVo> listSongsByPlaylistId(Long playlistId) {
        if (playlistId == null)
            return Collections.emptyList();
        return songMapper.selectSongsByPlaylistId(playlistId);
    }

    @Override
    public List<MyPlaylistVO> getMyPlaylists(Long userId) {
        // 1) 我创建的歌单
        List<Playlist> pls = this.list(
                new LambdaQueryWrapper<Playlist>()
                        .eq(Playlist::getCreatorUserId, userId)
                        .orderByDesc(Playlist::getUpdateTime)
        );
        if (pls == null || pls.isEmpty()) return Collections.emptyList();

        List<Long> ids = pls.stream().map(Playlist::getId).collect(Collectors.toList());

        // 2) 查 playlist_song，统计每个歌单的歌曲数
        List<PlaylistSong> ps = playlistSongMapper.selectList(
                new LambdaQueryWrapper<PlaylistSong>()
                        .in(PlaylistSong::getPlaylistId, ids)
        );

        Map<Long, Long> countMap = new HashMap<>();
        for (PlaylistSong x : ps) {
            countMap.merge(x.getPlaylistId(), 1L, Long::sum);
        }

        return pls.stream().map(p ->
            MyPlaylistVO.builder()
                .id(p.getId())
                .name(p.getName())
                .coverUrl(p.getCoverUrl())
                .description(p.getDescription())
                .songCount(countMap.getOrDefault(p.getId(), 0L))
                .build()
        ).toList();
    }
    private boolean isValidDateTimeFormat(String dateTimeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime.parse(dateTimeStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Override
    public List<PlaylistHomeVo> listByTags(List<Long> tagIds) {

        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 🔥 这里才是真正用到 listByTagsAnd 的地方
        List<Playlist> playlists =
                playlistMapper.listByTagsAnd(tagIds, tagIds.size());

        if (playlists.isEmpty()) {
            return Collections.emptyList();
        }

        // 转成前台需要的 VO（不要直接丢 Entity）
       return playlists.stream().map(p ->
    PlaylistHomeVo.builder()
        .id(p.getId())
        .name(p.getName())
        .coverUrl(p.getCoverUrl())
        .collectCount(p.getCollectCount())
        .build()
       ).toList();

    }

}
