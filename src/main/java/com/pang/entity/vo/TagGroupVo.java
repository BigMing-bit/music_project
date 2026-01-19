package com.pang.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class TagGroupVo {
    private Integer type;       // 1主题 2场景 3心情...
    private String typeName;    // 主题/场景/心情
    private List<TagVo> tags;
}
