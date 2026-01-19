package com.pang.entity.vo;

import lombok.Data;

@Data
public class SingerHomeVo {
    private Long id;
    private String name;
    private Integer gender;
    private String avatarUrl;

    private Long hotScore;
}
