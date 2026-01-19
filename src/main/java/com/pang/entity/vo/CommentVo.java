package com.pang.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVo {

    private Long id;

    private Long userId;
    private String nickname;
    private String avatar;

    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;



    private Long parentId;
    private Long rootId;

    private Long replyToUserId;
    private String replyContent;
    private String replyToNickname;
    private String replyToContent;

    private List<CommentVo> replies; // 二级回复（可选：后端聚合）
}
