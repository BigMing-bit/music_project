package com.pang.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


@Builder
@Data
public class AlbumDetailVo {
    private Long id;
    private String albumName;
    private String coverUrl;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;
    private Long singerId;
    private String singerName;
}
