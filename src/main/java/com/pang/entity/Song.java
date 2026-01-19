package com.pang.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("music_song")
public class Song {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String songName;
    private Long singerId;
    private Long albumId;
    private String albumName;
    private Integer durationSeconds;
    private String coverUrl;
    private String audioUrl;
    private String lyric;
    private Integer status;
    private Long playCount;
    private Long likeCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}