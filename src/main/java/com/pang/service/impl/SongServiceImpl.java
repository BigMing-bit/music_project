package com.pang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.Album;
import com.pang.entity.Song;
import com.pang.entity.vo.OptionVo;
import com.pang.entity.vo.SongVo;
import com.pang.mapper.AlbumMapper;
import com.pang.mapper.SongMapper;
import com.pang.security.dto.SongQueryDTO;
import com.pang.service.SongService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements SongService {

    private final SongMapper songMapper;
    private final AlbumMapper albumMapper;

    @Override
    public SongVo getSongDetail(Long id) {
        return songMapper.selectDetailById(id);
    }

    @Override
    public List<SongVo> getSongsBySinger(Long singerId) {
        return songMapper.selectBySingerId(singerId);
    }

    @Override
    public List<SongVo> getHotSongs(Integer limit) {
        return songMapper.selectHotSongs(limit);
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
        LambdaQueryWrapper<Song> wrapper = new LambdaQueryWrapper<Song>()
                .eq(Song::getStatus, 1)
                .like(StringUtils.hasText(keyword), Song::getSongName, keyword)
                .eq(singerId != null, Song::getSingerId, singerId)
                .eq(albumId != null, Song::getAlbumId, albumId)
                .orderByDesc("hot".equalsIgnoreCase(orderBy), Song::getPlayCount)
                .orderByDesc("like".equalsIgnoreCase(orderBy), Song::getLikeCount)
                .orderByDesc("hot".equalsIgnoreCase(orderBy) || "like".equalsIgnoreCase(orderBy), Song::getCreateTime);

        return this.page(p, wrapper);
    }

    @Override
    public IPage<SongVo> pageSongVo(SongQueryDTO queryDTO) {
        Page<SongVo> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        return songMapper.selectSongVoPage(page, queryDTO.getKeyword(), queryDTO.getStatus(), 
                queryDTO.getSingerId(), queryDTO.getAlbumId(), queryDTO.getOrderBy());
    }

    @Override
    public List<OptionVo> selectSongOptions(String keyword) {
        return songMapper.selectSongOptions(keyword);
    }

    @Override
    public List<OptionVo> selectSongOptionsByIds(List<Long> ids) {
        return CollectionUtils.isEmpty(ids) ? List.of() : songMapper.selectSongOptionsByIds(ids);
    }

    @Override
    public void updateAlbumName(Long songId) {
        Song song = songMapper.selectById(songId);
        if (song != null && song.getAlbumId() != null) {
            Album album = albumMapper.selectById(song.getAlbumId());
            if (album != null) {
                song.setAlbumName(album.getAlbumName());
                songMapper.updateById(song);
            }
        }
    }

    @Override
    public void saveOrUpdateWithAlbumName(Song song) {
        this.saveOrUpdate(song);
        updateAlbumName(song.getId());
    }
}
