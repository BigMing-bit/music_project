package com.pang.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PlaylistDetailVo {
    private Long id;
    private String name;
    private String description;
    private String coverUrl;

    private Long creatorUserId;
    private String creatorNickName;
    private Integer creatorRole;   // 0普通 1官方

    private Integer status;
    private Long playCount;
    private Long collectCount;

    private LocalDateTime createTime;

    private Boolean collected;
    private Long songCount;

    private List<TagVo> tags;
}
