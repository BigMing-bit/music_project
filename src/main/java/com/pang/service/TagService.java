package com.pang.service;

import com.pang.entity.vo.TagGroupVo;
import com.pang.entity.vo.TagVo;

import java.util.List;

public interface TagService {
    List<TagVo> listEnabled();
    List<TagGroupVo> groupEnabled();
}
