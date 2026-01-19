package com.pang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.SongSim;
import com.pang.mapper.SongSimMapper;
import com.pang.service.SongSimService;
import org.springframework.stereotype.Service;

@Service
public class SongSimServiceImpl extends ServiceImpl<SongSimMapper, SongSim> implements SongSimService {
}
