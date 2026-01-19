package com.pang.entity.vo;

import lombok.Data;

@Data
public class UserStatsVO {
    private Long likeSongCount;
    private Long likePlaylistCount;
    private Long likeAlbumCount;
    private Long playlistCount;
    private Long followCount; // 你没做关注表就先 0
    private Long fanCount;    // 你没做粉丝表就先 0
}