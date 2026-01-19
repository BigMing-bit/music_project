package com.pang.entity.vo;

import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class SongListVo {
    private Long id;
    private String songName;
    private Long singerId;
    private String singerName;
    private String singerAvatarUrl;
    private Long albumId;
    private String albumName;

    private String coverUrl;
    private String audioUrl;
    private Long playCount;
    private Long likeCount;
    private Integer durationSeconds;
}
