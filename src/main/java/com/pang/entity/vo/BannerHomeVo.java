package com.pang.entity.vo;

import lombok.Data;

@Data
public class BannerHomeVo {
    private Long id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private Integer sort;
    private Long score; // 用于 cursor，建议=sort
}

