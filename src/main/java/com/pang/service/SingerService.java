package com.pang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.entity.Singer;
import com.pang.entity.vo.*;
import com.pang.entity.vo.SingerVo;

import java.util.List;

public interface SingerService extends IService<Singer> {

    // ===== App 端 =====
    SingerVo getSingerDetail(Long id);
    CursorPageResult<SingerVo> getHotSingersCursor(String cursor, Integer size);
    CursorPageResult<SingerVo> getNewSingersCursor(String cursor, Integer size);
    List<SingerVo> getSimilarSingers(Long singerId, Integer limit);
    Page<SingerVo> getSingerPage(String name, Integer gender, Integer area, Integer style, Integer status, Integer page, Integer pageSize);
    Page<SongVo> getSingerSongsPage(Long singerId, String orderBy, Integer page, Integer pageSize);

    // ✅ App 下拉（只返回启用歌手）
    List<SelectOptionVo> selectSingerSelectOptions(String keyword);


    // ===== 管理端 =====
    IPage<Singer> adminPage(Integer pageNum, Integer pageSize, String keyword, Integer status, Integer gender);

    // ✅ 管理端下拉（OptionVo: value/label）
    List<OptionVo> adminSelectOptions(String keyword);
}
