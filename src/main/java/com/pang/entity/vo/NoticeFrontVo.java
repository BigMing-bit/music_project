package com.pang.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeFrontVo {
    private Long id;
    private String title;
    private String content;
    private String coverUrl;
    private String publisher;
    private LocalDateTime createTime;
}
