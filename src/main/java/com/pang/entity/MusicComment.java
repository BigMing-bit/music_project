package com.pang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("music_comment")
public class MusicComment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer targetType;
    private Long targetId;

    private Long parentId;     // 0=一级评论，否则=父评论id
    private Long rootId;       // 0=一级评论，否则=一级评论id

    private Long replyToUserId;
    private Long replyToCommentId;

    private Long userId;
    private String content;

    private Integer creatorType;      // 1用户 0官方
    private Long creatorAdminId;
    private String creatorAdminName;

    private String replyContent;
    private LocalDateTime replyTime;
    private Long replyAdminId;
    private String replyAdminName;

    private Integer status;     // 1正常 0删除/屏蔽
    private Integer likeCount;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

