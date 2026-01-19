package com.pang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.Song;
import com.pang.entity.vo.OptionVo;
import com.pang.entity.vo.SongVo;
import com.pang.mapper.SongMapper;
import com.pang.service.SongService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements SongService {

    private final SongMapper songMapper;

    public SongServiceImpl(SongMapper songMapper) {
        this.songMapper = songMapper;
    }

    // 根据ID查询歌曲详情
    @Override
    public SongVo getSongDetail(Long id) {
        return songMapper.selectDetailById(id);
    }

    @Override
    // 根据歌手ID查询歌曲
    public List<SongVo> getSongsBySinger(Long singerId) {
        return songMapper.selectBySingerId(singerId);
    }

    @Override
    public List<SongVo> getHotSongs(Integer limit) {
        try {
            return songMapper.selectHotSongs(limit);
        } catch (Exception e) {
            log.error("获取热门歌曲失败，limit: {}", limit, e);
            return null;
        }
    }

    @Override
    public List<SongVo> getNewSongs(Integer limit) {
        return songMapper.selectNewSongs(limit);
    }

    @Override
    public int incrementPlayCount(Long id) {
        return songMapper.incrementPlayCount(id);
    }

    @Override
    public Page<Song> pageSongs(Integer page, Integer pageSize,
                                String keyword, Long singerId, Long albumId, String orderBy) {

        Page<Song> p = new Page<>(page, pageSize);

        LambdaQueryWrapper<Song> wrapper = new LambdaQueryWrapper<>();

        // ✅ 默认查上架歌曲
        wrapper.eq(Song::getStatus, 1);

        // ✅ 关键词搜索（歌名）
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Song::getSongName, keyword);
        }

        // ✅ 歌手筛选
        if (singerId != null) {
            wrapper.eq(Song::getSingerId, singerId);
        }

        // ✅ 专辑筛选
        if (albumId != null) {
            wrapper.eq(Song::getAlbumId, albumId);
        }

        // ✅ 排序：hot/new/like
        if ("hot".equalsIgnoreCase(orderBy)) {
            wrapper.orderByDesc(Song::getPlayCount);
        } else if ("like".equalsIgnoreCase(orderBy)) {
            wrapper.orderByDesc(Song::getLikeCount);
        } else {
            wrapper.orderByDesc(Song::getCreateTime); // 默认最新
        }

        return this.page(p, wrapper);
    }

    @Override
    public IPage<SongVo> pageSongVo(Integer pageNum, Integer pageSize,
                                    String keyword, Integer status,
                                    Long singerId, Long albumId,
                                    String orderBy) {

        Page<SongVo> page = new Page<>(pageNum, pageSize);
        return songMapper.selectSongVoPage(page, keyword, status, singerId, albumId, orderBy);
    }

    @Override
    public List<OptionVo> selectSongOptions(String keyword) {
        return this.baseMapper.selectSongOptions(keyword);
    }

    @Override
    public List<OptionVo> selectSongOptionsByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return this.baseMapper.selectSongOptionsByIds(ids);
    }

}
