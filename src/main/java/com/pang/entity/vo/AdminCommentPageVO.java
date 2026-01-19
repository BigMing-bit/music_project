package com.pang.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminCommentPageVO {
    private Long id;
    private Long userId;
    private String userNickName;
    private Long parentId;

    private Long targetId;  // 目标 ID (歌曲、歌单、专辑的 ID)
    private Integer targetType;  // 目标类型：1-歌曲, 2-歌单, 3-专辑
    private String targetName;  // 动态目标名称，根据 targetType 显示不同内容（歌曲名、歌单名、专辑名）

    private String singerName;

    private String content;

    private String replyContent;
    private LocalDateTime replyTime;
    private String replyAdminName;

    private LocalDateTime createTime;

    private List<CommentVo> replies; // 子评论

    private Integer replied; // 标识是否已经回复，1为已回复，0为未回复

}
