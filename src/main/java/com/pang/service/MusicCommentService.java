package com.pang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.entity.MusicComment;
import com.pang.entity.vo.AdminCommentPageVO;
import com.pang.entity.vo.CommentVo;
import com.pang.security.dto.CommentCreateDTO;

import java.util.List;

public interface MusicCommentService extends IService<MusicComment> {

    IPage<AdminCommentPageVO> adminPage(Integer pageNum, Integer pageSize,
                                        String keyword, Long parentId, Integer targetType, Long targetId,
                                        Integer replied, String singerName);

    void reply(Long commentId, String replyContent, Long adminId, String adminName);

    boolean removeBatchByIdsSafe(List<Long> ids);

    IPage<CommentVo> pageComments(Integer targetType, Long targetId, int page, int pageSize);
    Long create(CommentCreateDTO dto, Long userId);

    List<CommentVo> getReplies(Long commentId);  // 获取评论的回复
}

