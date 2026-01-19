package com.pang.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class SongVo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String songName;
    private Long singerId;
    private String singerName;    // 歌手姓名
    private Long albumId;
    private String albumName;    //专辑名
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
