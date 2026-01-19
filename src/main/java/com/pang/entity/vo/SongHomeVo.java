package com.pang.entity.vo;

import lombok.Data;


@Data
public class SongHomeVo {
    private Long id;
    private String songName;
    private Long singerId;
    private String singerName;
    private Long albumId;
    private Integer durationSeconds;
    private String coverUrl;
    private String audioUrl;
    private Long playCount;
    private Long likeCount;
    private Long score;        // playCount + likeCount*3
}

