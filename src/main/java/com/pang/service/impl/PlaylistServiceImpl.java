package com.pang.service.impl;


import com.aliyun.core.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.*;
import com.pang.entity.vo.*;
import com.pang.mapper.*;
import com.pang.security.dto.PlaylistSaveDTO;
import com.pang.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pang.common.constants.BusinessConstants;

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
    private final PlaylistTagMapper playlistTagMapper;
    private final TagMapper tagMapper;

    @Override
    public CursorPageResult<PlaylistHomeVo> getHomePlaylists(String cursor, Integer size) {
        List<Playlist> playlists = queryHomePlaylists(cursor, size);

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
        result.setNextCursor(getNextHomeCursor(playlists));

        return result;
    }

    /**
     * 查询首页歌单列表
     */
    private List<Playlist> queryHomePlaylists(String cursor, Integer size) {
        LambdaQueryWrapper<Playlist> qw = new LambdaQueryWrapper<>();
        qw.eq(Playlist::getStatus, BusinessConstants.ACTIVE_STATUS);

        if (cursor != null && !cursor.isBlank()) {
            addCursorCondition(qw, cursor);
        }

        qw.orderByDesc(Playlist::getCollectCount)
                .orderByDesc(Playlist::getCreateTime)
                .last("LIMIT " + size);

        return playlistMapper.selectList(qw);
    }

    /**
     * 添加游标查询条件
     */
    private void addCursorCondition(LambdaQueryWrapper<Playlist> qw, String cursor) {
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

    /**
     * 获取下一页游标
     */
    private String getNextHomeCursor(List<Playlist> playlists) {
        return playlists.isEmpty() ? null : playlists.get(playlists.size() - 1).getCreateTime().toString();
    }

    @Override
    public PlaylistDetailVo getPlaylistDetail(Long playlistId, Long userId) {
        PlaylistDetailVo vo = playlistMapper.selectDetail(playlistId);
        if (vo == null) return null;

        vo.setCollected(isUserFavorited(userId, playlistId));
        vo.setTags(tagMapper.listByPlaylistId(playlistId));

        return vo;
    }

    /**
     * 判断用户是否收藏了歌单
     */
    private boolean isUserFavorited(Long userId, Long playlistId) {
        return userId != null && playlistMapper.countUserFavorite(userId, playlistId) > 0;
    }

    // ✅ 歌单里的歌曲列表（点击歌单进去才查）
    @Override
    public Page<SongListVo> getPlaylistSongs(Long playlistId, Integer page, Integer pageSize) {
        Page<PlaylistSong> psPage = queryPlaylistSongPage(playlistId, page, pageSize);

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

        List<SongListVo> voList = buildSongVoList(songIds);
        result.setRecords(voList);
        return result;
    }

    /**
     * 查询歌单歌曲分页
     */
    private Page<PlaylistSong> queryPlaylistSongPage(Long playlistId, Integer page, Integer pageSize) {
        return playlistSongMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<PlaylistSong>()
                        .eq(PlaylistSong::getPlaylistId, playlistId)
                        .orderByAsc(PlaylistSong::getSort)
                        .orderByAsc(PlaylistSong::getId)
        );
    }

    /**
     * 构建歌曲 VO 列表
     */
    private List<SongListVo> buildSongVoList(List<Long> songIds) {
        List<Song> songs = songMapper.selectList(
                new LambdaQueryWrapper<Song>()
                        .in(Song::getId, songIds)
                        .eq(Song::getStatus, BusinessConstants.ACTIVE_STATUS)
        );

        Map<Long, String> singerMap = getSingerNameMap(
                songs.stream().map(Song::getSingerId).filter(Objects::nonNull).distinct().toList()
        );

        // 按原始顺序排序
        Map<Long, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < songIds.size(); i++) {
            orderMap.put(songIds.get(i), i);
        }
        songs.sort(Comparator.comparingInt(s -> orderMap.getOrDefault(s.getId(), Integer.MAX_VALUE)));

        return songs.stream().map(s ->
                SongListVo.builder()
                        .id(s.getId())
                        .songName(s.getSongName())
                        .singerName(singerMap.getOrDefault(s.getSingerId(), BusinessConstants.UNKNOWN_SINGER))
                        .coverUrl(s.getCoverUrl())
                        .audioUrl(s.getAudioUrl())
                        .playCount(s.getPlayCount())
                        .likeCount(s.getLikeCount())
                        .durationSeconds(s.getDurationSeconds())
                        .build()
        ).toList();
    }

    private Map<Long, String> getSingerNameMap(List<Long> singerIds) {
        if (singerIds == null || singerIds.isEmpty()) return new HashMap<>();

        List<Singer> singers = singerMapper.selectList(
                new LambdaQueryWrapper<Singer>()
                        .in(Singer::getId, singerIds)
                        .eq(Singer::getStatus, BusinessConstants.ACTIVE_STATUS)
        );

        return singers.stream()
                .collect(Collectors.toMap(Singer::getId, Singer::getName, (a, b) -> a));
    }

    @Override
    public CursorPageResult<SongListVo> getPlaylistSongsCursor(Long playlistId, String cursor, Integer size) {
        List<PlaylistSong> psList = queryPlaylistSongsWithCursor(playlistId, cursor, size);

        List<Long> songIds = psList.stream().map(PlaylistSong::getSongId).toList();

        CursorPageResult<SongListVo> result = new CursorPageResult<>();
        result.setHasMore(psList.size() == size);

        if (songIds.isEmpty()) {
            result.setList(Collections.emptyList());
            result.setNextCursor(null);
            return result;
        }

        List<SongListVo> voList = buildSongVoList(songIds);
        result.setList(voList);
        result.setNextCursor(buildNextCursor(psList));

        return result;
    }

    /**
     * 查询带游标的歌单歌曲
     */
    private List<PlaylistSong> queryPlaylistSongsWithCursor(Long playlistId, String cursor, Integer size) {
        LambdaQueryWrapper<PlaylistSong> qw = new LambdaQueryWrapper<>();
        qw.eq(PlaylistSong::getPlaylistId, playlistId);

        if (cursor != null && !cursor.isBlank()) {
            addPlaylistSongCursorCondition(qw, cursor);
        }

        qw.orderByAsc(PlaylistSong::getSort)
                .orderByAsc(PlaylistSong::getId)
                .last("LIMIT " + size);

        return playlistSongMapper.selectList(qw);
    }

    /**
     * 添加歌单歌曲游标条件
     */
    private void addPlaylistSongCursorCondition(LambdaQueryWrapper<PlaylistSong> qw, String cursor) {
        String[] arr = cursor.split(BusinessConstants.CURSOR_SEPARATOR);
        if (arr.length >= 2) {
            Integer cursorSort = Integer.parseInt(arr[0]);
            Long cursorId = Long.parseLong(arr[1]);

            qw.and(w -> w.gt(PlaylistSong::getSort, cursorSort)
                    .or()
                    .eq(PlaylistSong::getSort, cursorSort)
                    .gt(PlaylistSong::getId, cursorId));
        }
    }

    /**
     * 构建下一页游标
     */
    private String buildNextCursor(List<PlaylistSong> psList) {
        if (psList.isEmpty()) return null;
        PlaylistSong last = psList.get(psList.size() - 1);
        return last.getSort() + BusinessConstants.CURSOR_SEPARATOR + last.getId();
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
        validateToggleFavoriteParams(playlistId, userId);

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

    /**
     * 验证切换收藏参数
     */
    private void validateToggleFavoriteParams(Long playlistId, Long userId) {
        if (playlistId == null || userId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
    }

    @Override
    public boolean isFavorited(Long playlistId, Long userId) {
        if (userId == null) return false;
        return baseMapper.countUserFavorite(userId, playlistId) > 0;
    }

    @Override
    public IPage<PlaylistVo> pageVo(int pageNum, int pageSize, String keyword, Integer status) {
        log.info("Calling pageVo with pageNum={}, pageSize={}, keyword={}, status={}", pageNum, pageSize, keyword, status);
        try {
            Page<PlaylistVo> page = new Page<>(pageNum, pageSize);
            IPage<PlaylistVo> result = this.baseMapper.pageVo(page, keyword, status);
            log.info("pageVo returned: total={}, records={}", result.getTotal(), result.getRecords().size());
            return result;
        } catch (Exception e) {
            log.error("Error in pageVo: ", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public Long saveOrUpdateWithSongs(PlaylistSaveDTO dto) {
        Playlist playlist = buildPlaylistFromDto(dto);
        this.saveOrUpdate(playlist);

        Long playlistId = playlist.getId();
        updatePlaylistSongs(playlistId, dto.getSongIds());
        updatePlaylistTags(playlistId, dto.getTagIds());

        return playlistId;
    }

    /**
     * 从 DTO 构建歌单实体
     */
    private Playlist buildPlaylistFromDto(PlaylistSaveDTO dto) {
        Playlist p;

        if (dto.getId() != null) {
            Playlist db = this.getById(dto.getId());
            if (db == null) throw new RuntimeException("歌单不存在");

            p = new Playlist();
            p.setId(db.getId());
            p.setCreatorUserId(db.getCreatorUserId()); // 创建者永远不变
        } else {
            p = new Playlist();
            if (dto.getCreatorUserId() == null) throw new RuntimeException("creatorUserId不能为空");
            p.setCreatorUserId(dto.getCreatorUserId());
        }

        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setCoverUrl(dto.getCoverUrl());
        p.setStatus(dto.getStatus() == null ? BusinessConstants.ACTIVE_STATUS : dto.getStatus());
        p.setUpdateTime(LocalDateTime.now());

        return p;
    }

    /**
     * 更新歌单歌曲
     */
    private void updatePlaylistSongs(Long playlistId, List<Long> songIds) {
        playlistSongMapper.deleteByPlaylistId(playlistId);
        if (songIds != null && !songIds.isEmpty()) {
            playlistSongMapper.insertBatch(playlistId, songIds);
        }
    }

    /**
     * 更新歌单标签
     */
    private void updatePlaylistTags(Long playlistId, List<Long> tagIds) {
        if (tagIds != null) {
            playlistTagMapper.deleteByPlaylistId(playlistId);
            if (!tagIds.isEmpty()) {
                playlistTagMapper.insertBatch(playlistId, tagIds);
            }
        }
    }

    @Override
    @Transactional
    public void editPlaylist(Long playlistId, PlaylistSaveDTO dto, Long operatorUserId) {
        validatePlaylistExists(playlistId);
        validateUserExists(operatorUserId);

        Playlist db = this.getById(playlistId);
        User me = userMapper.selectById(operatorUserId);

        validatePlaylistEditPermission(me, db, operatorUserId);

        dto.setId(playlistId);
        dto.setCreatorUserId(db.getCreatorUserId());

        this.saveOrUpdateWithSongs(dto);
    }

    /**
     * 验证歌单是否存在
     */
    private void validatePlaylistExists(Long playlistId) {
        if (this.getById(playlistId) == null) {
            throw new RuntimeException("歌单不存在");
        }
    }

    /**
     * 验证用户是否存在
     */
    private void validateUserExists(Long userId) {
        if (userMapper.selectById(userId) == null) {
            throw new RuntimeException("用户不存在");
        }
    }

    /**
     * 验证歌单编辑权限
     */
    private void validatePlaylistEditPermission(User me, Playlist db, Long operatorUserId) {
        int myRole = me.getRole() == null ? 0 : me.getRole();
        Long creatorUserId = db.getCreatorUserId();
        User creator = creatorUserId == null ? null : userMapper.selectById(creatorUserId);
        int creatorRole = (creator == null || creator.getRole() == null) ? 0 : creator.getRole();

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
    }

    @Override
    public List<PlaylistSongItemVo> listSongsByPlaylistId(Long playlistId) {
        if (playlistId == null) return Collections.emptyList();
        return songMapper.selectSongsByPlaylistId(playlistId);
    }

    @Override
    public List<MyPlaylistVO> getMyPlaylists(Long userId) {
        List<Playlist> playlists = queryUserPlaylists(userId);
        if (playlists.isEmpty()) return Collections.emptyList();

        List<Long> ids = playlists.stream().map(Playlist::getId).collect(Collectors.toList());
        Map<Long, Long> countMap = getSongCountPerPlaylist(ids);

        return playlists.stream().map(p ->
                MyPlaylistVO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .coverUrl(p.getCoverUrl())
                        .description(p.getDescription())
                        .songCount(countMap.getOrDefault(p.getId(), 0L))
                        .build()
        ).toList();
    }

    /**
     * 查询用户创建的歌单
     */
    private List<Playlist> queryUserPlaylists(Long userId) {
        return this.list(
                new LambdaQueryWrapper<Playlist>()
                        .eq(Playlist::getCreatorUserId, userId)
                        .orderByDesc(Playlist::getUpdateTime)
        );
    }

    /**
     * 获取每个歌单的歌曲数量
     */
    private Map<Long, Long> getSongCountPerPlaylist(List<Long> playlistIds) {
        List<PlaylistSong> playlistSongs = playlistSongMapper.selectList(
                new LambdaQueryWrapper<PlaylistSong>()
                        .in(PlaylistSong::getPlaylistId, playlistIds)
        );

        Map<Long, Long> countMap = new HashMap<>();
        for (PlaylistSong ps : playlistSongs) {
            countMap.merge(ps.getPlaylistId(), 1L, Long::sum);
        }
        return countMap;
    }

    private boolean isValidDateTimeFormat(String dateTimeStr) {
        if (StringUtils.isBlank(dateTimeStr)) return false;
        try {
            LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return true;
        } catch (DateTimeParseException e) {
            log.warn("Invalid datetime format: {}", dateTimeStr);
            return false;
        }
    }

    @Override
    public List<PlaylistHomeVo> listByTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Playlist> playlists = playlistMapper.listByTagsAnd(tagIds, tagIds.size());
        if (playlists.isEmpty()) {
            return Collections.emptyList();
        }

        return playlists.stream().map(p ->
                PlaylistHomeVo.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .coverUrl(p.getCoverUrl())
                        .description(p.getDescription())
                        .playCount(p.getPlayCount())
                        .collectCount(p.getCollectCount())
                        .build()
        ).toList();
    }
}

