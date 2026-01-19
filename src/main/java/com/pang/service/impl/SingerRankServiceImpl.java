package com.pang.service.impl;

import com.pang.entity.vo.SingerRankRowVO;
import com.pang.mapper.SingerRankMapper;
import com.pang.service.SingerRankService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

@Service
public class SingerRankServiceImpl implements SingerRankService {

    private final SingerRankMapper singerRankMapper;

    public SingerRankServiceImpl(SingerRankMapper singerRankMapper) {
        this.singerRankMapper = singerRankMapper;
    }

    /**
     * ✅ 生成总榜
     * rank_type=1, rank_date=NULL
     */
    @Override
    @Transactional
    public void generateAllTimeRank() {
        Integer type = 1;
        Date rankDate = null;

        // 权重：你可以后期改成配置
        int playW = 1;
        int likeW = 30;

        singerRankMapper.clearRank(type, rankDate);
        singerRankMapper.buildRank(type, rankDate, playW, likeW);
        singerRankMapper.refreshRankNo(type, rankDate);
    }

    /**
     * ✅ 查询总榜 Top N
     */
    @Override
    public List<SingerRankRowVO> topAllTime(Integer size) {
        if (size == null || size < 1) size = 50;
        return singerRankMapper.top(1, null, size);
    }
}
