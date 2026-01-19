package com.pang.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.Notice;
import com.pang.entity.vo.NoticeDetailVo;
import com.pang.entity.vo.NoticeFrontVo;
import com.pang.entity.vo.NoticeVo;
import com.pang.mapper.NoticeMapper;
import com.pang.service.NoticeService;
import org.springframework.stereotype.Service;



@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Override
    public IPage<NoticeVo> adminPage(Integer pageNum, Integer pageSize, String keyword, Integer status, Integer visible) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        return baseMapper.adminPage(new Page<>(pageNum, pageSize), keyword, status, visible);
    }

    @Override
    public java.util.List<NoticeFrontVo> frontLatest(Integer size) {
        if (size == null || size < 1) size = 10;
        return baseMapper.frontLatest(size);
    }

    @Override
    public NoticeDetailVo frontDetail(Long id) {
        return baseMapper.frontDetail(id);
    }

    @Override
    public NoticeVo adminDetail(Long id) {
        return baseMapper.adminDetail(id);
    }

}
