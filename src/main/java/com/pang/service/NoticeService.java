package com.pang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.entity.Notice;
import com.pang.entity.vo.NoticeDetailVo;
import com.pang.entity.vo.NoticeFrontVo;
import com.pang.entity.vo.NoticeVo;

import java.util.List;

public interface NoticeService extends IService<Notice> {
    IPage<NoticeVo> adminPage(Integer pageNum, Integer pageSize, String keyword, Integer status, Integer visible);
    List<NoticeFrontVo> frontLatest(Integer size);
    NoticeDetailVo frontDetail(Long id);

    NoticeVo adminDetail(Long id);
}
