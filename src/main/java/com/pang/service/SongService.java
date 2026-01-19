package com.pang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.entity.Song;
import com.pang.entity.vo.OptionVo;
import com.pang.entity.vo.SongVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SongService extends IService<Song> {


    SongVo getSongDetail(@Param("id") Long id);

    List<SongVo> getSongsBySinger(@Param("singerId") Long singerId);


    List<SongVo> getHotSongs(@Param("limit") Integer limit);


    List<SongVo> getNewSongs(@Param("limit") Integer limit);

    int incrementPlayCount(@Param("id") Long id);

    Page<Song> pageSongs(Integer page, Integer pageSize,
                         String keyword, Long singerId, Long albumId, String orderBy);

    IPage<SongVo> pageSongVo(Integer pageNum, Integer pageSize,
                             String keyword, Integer status,
                             Long singerId, Long albumId,
                             String orderBy);


    List<OptionVo> selectSongOptions(String keyword);


    List<OptionVo> selectSongOptionsByIds(List<Long> ids);


}
