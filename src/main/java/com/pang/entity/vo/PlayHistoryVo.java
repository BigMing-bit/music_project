package com.pang.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PlayHistoryVo {
    private Long historyId;
    private Long songId;
    private String songName;
    private Long singerId;
    private String singerName;
    private String coverUrl;
    private String audioUrl;
    private Integer durationSeconds;
    private LocalDateTime playTime;
}
