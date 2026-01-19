package com.pang.security.dto;

import lombok.Data;

@Data
public class CommentCreateDTO {
    private Integer targetType; // 1歌曲 2歌单 3专辑
    private Long targetId;

    private Long parentId; // 回复时传父评论id；一级评论传0或null
    private Long replyToUserId;
    private Long replyToCommentId;

    private String content;
}
