package com.pang.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PlaylistVo {

    private Long id;
    private String name;
    private String description;
    private Long singerId;
    private Long songId;
    private String singerName;
    private String coverUrl;
    private Integer status;
    private Long collectCount;
    private Long playCount;
    private LocalDateTime createTime;
    private Integer isOfficial;
}
