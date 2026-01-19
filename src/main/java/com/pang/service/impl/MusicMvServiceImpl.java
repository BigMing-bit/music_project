package com.pang.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.MusicMv;
import com.pang.entity.vo.AdminMvPageVO;
import com.pang.mapper.MusicMvMapper;
import com.pang.service.MusicMvService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MusicMvServiceImpl extends ServiceImpl<MusicMvMapper, MusicMv>
        implements MusicMvService {


    @Override
    public IPage<AdminMvPageVO> adminPage(Integer pageNum, Integer pageSize,
                                          String keyword, Long singerId, Integer status) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        return baseMapper.adminPage(new Page<>(pageNum, pageSize), keyword, singerId, status);
    }

    @Override
    public AdminMvPageVO adminDetail(Long id) {
        return baseMapper.adminDetail(id);
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        if (id == null || status == null) return false;
        return baseMapper.updateStatus(id, status) > 0;
    }

    @Override
    @Transactional
    public boolean removeBatchByIdsSafe(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return true;
        return this.removeBatchByIds(ids);
    }

    @Override
    @Transactional
    public boolean save(MusicMv entity) {
        if (entity.getCreateTime() == null) entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return super.save(entity);
    }

    @Override
    @Transactional
    public boolean updateById(MusicMv entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return baseMapper.updateById(entity) > 0;
    }
}
