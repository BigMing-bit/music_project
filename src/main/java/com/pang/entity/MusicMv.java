package com.pang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("music_mv")
public class MusicMv {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String mvName;
    private Long singerId;

    private String coverUrl;
    private String videoUrl;

    private Integer durationSeconds;
    private Long playCount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;

    private Integer sort;
    private Integer status; // 1启用 0禁用

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
