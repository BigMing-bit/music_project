package com.pang.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.Singer;
import com.pang.entity.vo.*;
import com.pang.mapper.SingerMapper;
import com.pang.service.SingerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SingerServiceImpl extends ServiceImpl<SingerMapper, Singer> implements SingerService {

    private final SingerMapper singerMapper;

    @Override
    public SingerVo getSingerDetail(Long id) {
        SingerVo singer = singerMapper.selectSingerDetail(id);
        if (singer != null) {
            processSingerVo(singer);
        }
        return singer;
    }

    @Override
    public List<SingerVo> getSimilarSingers(Long singerId, Integer limit) {
        if (limit == null || limit <= 0) limit = 5;

        Singer singer = this.getById(singerId);
        if (singer == null) return List.of();

        List<SingerVo> list = singerMapper.selectSimilarSingers(singerId, singer.getGender(), limit);
        list.forEach(this::processSingerVo);
        return list;
    }

    @Override
    public Page<SingerVo> getSingerPage(String name, Integer gender, Integer area, Integer style, Integer status, Integer page, Integer pageSize) {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 30;

        LambdaQueryWrapper<Singer> qw = new LambdaQueryWrapper<>();
        qw.like(StrUtil.isNotBlank(name), Singer::getName, name)
                .eq(gender != null, Singer::getGender, gender)
                .eq(area != null, Singer::getArea, area)
                .eq(style != null, Singer::getStyle, style)
                .eq(status != null, Singer::getStatus, status)
                .orderByDesc(Singer::getCreateTime);

        Page<Singer> p = this.page(new Page<>(page, pageSize), qw);

        Page<SingerVo> voPage = new Page<>();
        voPage.setCurrent(p.getCurrent());
        voPage.setSize(p.getSize());
        voPage.setTotal(p.getTotal());
        voPage.setPages(p.getPages());

        List<SingerVo> voList = p.getRecords().stream().map(s -> {
            SingerVo vo = new SingerVo();
            vo.setId(s.getId());
            vo.setName(s.getName());
            vo.setGender(s.getGender());
            vo.setArea(s.getArea());
            vo.setStyle(s.getStyle());
            vo.setAvatarUrl(s.getAvatarUrl());
            vo.setIntro(s.getIntro());
            vo.setStatus(s.getStatus());
            vo.setCreateTime(s.getCreateTime());
            vo.setUpdateTime(s.getUpdateTime());
            processSingerVo(vo);
            return vo;
        }).toList();

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Page<SongVo> getSingerSongsPage(Long singerId, String orderBy, Integer page, Integer pageSize) {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 20;

        Long total = baseMapper.countSongsBySingerId(singerId);
        int offset = (page - 1) * pageSize;

        List<SongVo> songs = baseMapper.selectSongsBySingerIdPage(singerId, offset, pageSize);
        if (StrUtil.isNotBlank(orderBy)) {
            sortSongs(songs, orderBy);
        }

        Page<SongVo> result = new Page<>();
        result.setCurrent(page);
        result.setSize(pageSize);
        result.setTotal(total);
        result.setPages((int) Math.ceil((double) total / pageSize));
        result.setRecords(songs);
        return result;
    }

    // ✅ 管理端分页：Singer 实体
    @Override
    public IPage<Singer> adminPage(Integer pageNum, Integer pageSize, String keyword, Integer status, Integer gender) {
        Page<Singer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Singer> qw = new LambdaQueryWrapper<>();

        qw.like(keyword != null && !keyword.trim().isEmpty(), Singer::getName, keyword)
                .eq(status != null, Singer::getStatus, status)
                .eq(gender != null, Singer::getGender, gender)
                .orderByDesc(Singer::getId);

        return this.page(page, qw);
    }

    @Override
    public List<OptionVo> adminSelectOptions(String keyword) {
        return this.baseMapper.selectOptions(keyword);
    }

    private void processSingerVo(SingerVo singer) {
        if (singer.getGender() != null) {
            singer.setGenderText(singer.getGender() == 1 ? "男" : singer.getGender() == 2 ? "女" : "组合");
        }
        if (singer.getArea() != null) {
            switch (singer.getArea()) {
                case 1: singer.setAreaText("内地"); break;
                case 2: singer.setAreaText("港台"); break;
                case 3: singer.setAreaText("欧美"); break;
                case 4: singer.setAreaText("日本"); break;
                case 5: singer.setAreaText("韩国"); break;
                default: singer.setAreaText("未知"); break;
            }
        }
        if (singer.getStyle() != null) {
            switch (singer.getStyle()) {
                case 1: singer.setStyleText("流行"); break;
                case 2: singer.setStyleText("说唱"); break;
                case 3: singer.setStyleText("国风"); break;
                case 4: singer.setStyleText("摇滚"); break;
                case 5: singer.setStyleText("电子"); break;
                case 6: singer.setStyleText("民谣"); break;
                case 7: singer.setStyleText("R&B"); break;
                case 8: singer.setStyleText("民族乐"); break;
                case 9: singer.setStyleText("轻音乐"); break;
                case 10: singer.setStyleText("爵士"); break;
                case 11: singer.setStyleText("古典"); break;
                case 12: singer.setStyleText("乡村"); break;
                case 13: singer.setStyleText("蓝调"); break;
                default: singer.setStyleText("未知"); break;
            }
        }
        if (singer.getStatus() != null) {
            singer.setStatusText(singer.getStatus() == 1 ? "启用" : "禁用");
        }
        if (StrUtil.isBlank(singer.getAvatarUrl())) {
            singer.setAvatarUrl("/default-singer-avatar.jpg");
        }
    }

    private void sortSongs(List<SongVo> songs, String orderBy) {
        if (songs == null || songs.isEmpty()) return;

        switch (orderBy.toLowerCase()) {
            case "hot" -> songs.sort((a, b) -> Long.compare(
                    b.getPlayCount() == null ? 0 : b.getPlayCount(),
                    a.getPlayCount() == null ? 0 : a.getPlayCount()
            ));
            case "like" -> songs.sort((a, b) -> Long.compare(
                    b.getLikeCount() == null ? 0 : b.getLikeCount(),
                    a.getLikeCount() == null ? 0 : a.getLikeCount()
            ));
            default -> songs.sort((a, b) -> {
                if (a.getCreateTime() == null && b.getCreateTime() == null) return 0;
                if (a.getCreateTime() == null) return 1;
                if (b.getCreateTime() == null) return -1;
                return b.getCreateTime().compareTo(a.getCreateTime());
            });
        }
    }
}
