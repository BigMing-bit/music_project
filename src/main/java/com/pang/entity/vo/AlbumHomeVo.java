package com.pang.entity.vo;

import lombok.Data;


@Data
public class AlbumHomeVo {
    private Long id;
    private String albumName;
    private Long singerId;
    private String singerName;
    private String coverUrl;
    private Long collectCount;
    private Long score;        // 用于 cursor（本质=collectCount）
}

