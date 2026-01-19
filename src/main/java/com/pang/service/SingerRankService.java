package com.pang.service;

import com.pang.entity.vo.SingerRankRowVO;

import java.util.List;

public interface SingerRankService {

    /** 生成歌手总榜（清空→重算→写rankNo） */
    void generateAllTimeRank();

    /** 获取歌手总榜Top N */
    List<SingerRankRowVO> topAllTime(Integer size);
}
