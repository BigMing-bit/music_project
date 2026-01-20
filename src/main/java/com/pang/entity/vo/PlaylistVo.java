package com.pang.entity.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class PlaylistVo {

    private Long id;
    private String name;
    private String description;
    private String coverUrl;
    private Integer status;
    private Long collectCount;
    private Long playCount;
    private LocalDateTime createTime;
    private Long creatorUserId;
    private String singerName;
}
