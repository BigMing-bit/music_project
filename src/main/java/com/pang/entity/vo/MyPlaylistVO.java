package com.pang.entity.vo;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MyPlaylistVO {
    private Long id;
    private String name;
    private String coverUrl;
    private String description;
    private Long songCount;
}
