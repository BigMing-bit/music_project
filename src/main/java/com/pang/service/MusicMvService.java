package com.pang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.entity.MusicMv;
import com.pang.entity.vo.AdminMvPageVO;

import java.util.List;

public interface MusicMvService extends IService<MusicMv> {

    IPage<AdminMvPageVO> adminPage(Integer pageNum, Integer pageSize,
                                   String keyword, Long singerId, Integer status);

    AdminMvPageVO adminDetail(Long id);

    boolean updateStatus(Long id, Integer status);

    boolean removeBatchByIdsSafe(List<Long> ids);
}
