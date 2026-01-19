package com.pang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.PlaylistSim;
import com.pang.mapper.PlaylistSimMapper;
import com.pang.service.PlaylistSimService;
import org.springframework.stereotype.Service;

@Service
public class PlaylistSimServiceImpl extends ServiceImpl<PlaylistSimMapper, PlaylistSim> implements PlaylistSimService {
}
