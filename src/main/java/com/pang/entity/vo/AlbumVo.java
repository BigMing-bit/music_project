package com.pang.entity.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class AlbumVo {

    private Long id;
    private Long albumId;
    private String albumName;
    private Long singerId;
    private String singerName;
    private String coverUrl;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;
    private Long collectCount;



    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
