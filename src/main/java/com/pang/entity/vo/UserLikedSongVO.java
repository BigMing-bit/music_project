package com.pang.entity.vo;


import lombok.Data;

@Data
public class UserLikedSongVO {
    private Long id;              // songId
    private String songName;
    private Integer durationSeconds;
    private String coverUrl;
    private String audioUrl;

    // 先预留，等你有 Singer/Album 再补
    private Long singerId;
    private Long albumId;
    private String singerName;
    private String albumName;

}

