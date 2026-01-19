package com.pang.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class AdminMenuVo {
    private Long id;
    private String name;
    private String path;
    private String component;
    private String icon;
    private String permission;
    private List<AdminMenuVo> children;
}
