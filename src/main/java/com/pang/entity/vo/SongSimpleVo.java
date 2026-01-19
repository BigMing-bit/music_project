package com.pang.entity.vo;

import lombok.Data;

@Data
public class SongSimpleVo {
    private Long id;
    private String songName;
    private Long singerId;
    private String singerName;
    private Long albumId;
    private String albumName;
    private Integer durationSeconds;
    private String coverUrl;
    private String audioUrl;
}
