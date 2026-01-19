package com.pang.entity.vo;


import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class PlaylistHomeVo {
    private Long id;
    private String name;
    private String description;
    private String coverUrl;
    private Long playCount;
    private Long collectCount;
    private Long score;        // 用于 cursor
}
