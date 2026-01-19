package com.pang.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeHomeVo {
    private Long id;
    private String title;
    private String coverUrl;
    private String publisher;
    private LocalDateTime createTime;
    private Integer sort;
    private Long score; // 用于 cursor，建议=sort
}
