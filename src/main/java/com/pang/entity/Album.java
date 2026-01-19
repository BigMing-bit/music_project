package com.pang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("music_album")
public class Album {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String albumName;
    private Long singerId;
    private String coverUrl;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;

    private Long collectCount;

    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
