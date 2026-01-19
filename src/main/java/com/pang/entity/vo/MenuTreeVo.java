package com.pang.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuTreeVo {
    private Long id;
    private Long parentId;
    private Integer menuType;
    private String menuName;
    private String path;
    private String component;
    private String icon;
    private Integer sort;
    private List<MenuTreeVo> children = new ArrayList<>();
}
