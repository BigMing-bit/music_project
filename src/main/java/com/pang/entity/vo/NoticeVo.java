package com.pang.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeVo {
    private Long id;
    private String title;
    private String coverUrl;
    private String content;
    private String publisher;
    private Integer sort;
    private Integer visible;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private LocalDateTime createTime;
}
