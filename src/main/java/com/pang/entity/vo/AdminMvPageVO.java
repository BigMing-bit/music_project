package com.pang.entity.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AdminMvPageVO {
    private Long id;
    private String mvName;

    private Long singerId;
    private String singerName;   // 联表拿

    private String coverUrl;
    private String videoUrl;

    private Integer durationSeconds;
    private Long playCount;

    private LocalDate publishDate;
    private Integer sort;
    private Integer status;

    private LocalDateTime createTime;
}
