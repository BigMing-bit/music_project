package com.pang.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.MusicComment;
import com.pang.entity.SysOperationLog;
import com.pang.entity.vo.AdminCommentPageVO;
import com.pang.entity.vo.CommentVo;
import com.pang.mapper.MusicCommentMapper;
import com.pang.mapper.SysAdminMapper;
import com.pang.security.dto.CommentCreateDTO;
import com.pang.service.MusicCommentService;
import com.pang.service.SysOperationLogService;
import jakarta.annotation.Resource;
import com.pang.common.Constants.CommentConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MusicCommentServiceImpl extends ServiceImpl<MusicCommentMapper, MusicComment> implements MusicCommentService {

    @Resource
    private SysAdminMapper sysAdminMapper;

    @Resource
    private MusicCommentMapper commentMapper;


    @Autowired
    private SysOperationLogService logService;


    @Override
    @Transactional
    public void reply(Long commentId, String replyContent, Long adminId, String adminName) {
        validateReplyParams(commentId, replyContent);

        String resolvedAdminName = resolveAdminName(adminId, adminName);
        executeReply(commentId, replyContent, adminId, resolvedAdminName);
    }

    private void validateReplyParams(Long commentId, String replyContent) {
        if (commentId == null) {
            throw new RuntimeException(CommentConstants.COMMENT_ID_REQUIRED);
        }
        if (replyContent == null || replyContent.isBlank()) {
            throw new RuntimeException(CommentConstants.REPLY_CONTENT_REQUIRED);
        }
    }

    private String resolveAdminName(Long adminId, String adminName) {
        if (adminName != null && !adminName.isBlank()) {
            return adminName;
        }

        String fetchedName = sysAdminMapper.selectDisplayName(adminId);
        return (fetchedName != null && !fetchedName.isBlank()) ? fetchedName : CommentConstants.DEFAULT_ADMIN_NAME;
    }

    private void executeReply(Long commentId, String replyContent, Long adminId, String adminName) {
        int result = baseMapper.replyComment(commentId, replyContent.trim(), adminId, adminName);
        if (result <= 0) {
            throw new RuntimeException(CommentConstants.REPLY_FAILED_ERROR);
        }
    }


    @Override
    @Transactional
    public boolean removeBatchByIdsSafe(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        ids.stream()
                .filter(Objects::nonNull)
                .forEach(id -> baseMapper.deleteCommentAndChildren(id));

        return true;
    }

    @Override
    public IPage<CommentVo> pageComments(Integer targetType, Long targetId, int page, int pageSize) {
        Page<CommentVo> p = new Page<>(page, pageSize);
        IPage<CommentVo> roots = commentMapper.pageRoot(p, targetType, targetId);

        List<CommentVo> rootList = roots.getRecords();
        if (!rootList.isEmpty()) {
            // 获取每个根评论的回复
            for (CommentVo root : rootList) {
                // 查询该根评论的所有回复
                List<CommentVo> replies = commentMapper.listReplies(root.getId());  // 使用 root.getId() 来查询
                root.setReplies(replies);  // 为该根评论添加回复
            }
        }
        return roots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CommentCreateDTO dto, Long userId) {
            validateCommentCreateDTO(dto);

            MusicComment comment = buildMusicComment(dto, userId);
            setParentCommentRelationship(comment, dto);
            commentMapper.insert(comment);
            updateRootIdIfRootComment(comment);

            return comment.getId();
    }

    private void validateCommentCreateDTO(CommentCreateDTO dto) {
        if (dto.getTargetType() == null || dto.getTargetId() == null) {
            throw new RuntimeException("targetType/targetId不能为空");
        }
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new RuntimeException("内容不能为空");
        }
    }

    private MusicComment buildMusicComment(CommentCreateDTO dto, Long userId) {
        MusicComment comment = new MusicComment();
        comment.setTargetType(dto.getTargetType());
        comment.setTargetId(dto.getTargetId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent().trim());
        comment.setStatus(1);
        comment.setLikeCount(0);
        comment.setCreatorType(1);

        long parentId = dto.getParentId() != null ? dto.getParentId() : 0L;
        comment.setParentId(parentId);

        return comment;
    }

    private void setParentCommentRelationship(MusicComment comment, CommentCreateDTO dto) {
        long parentId = comment.getParentId();

        if (parentId == 0) {
            comment.setRootId(0L);
        } else {
            handleReplyRelationship(comment, dto, parentId);
        }
    }

    private void handleReplyRelationship(MusicComment comment, CommentCreateDTO dto, long parentId) {
        MusicComment parent = commentMapper.selectById(parentId);
        if (parent == null) {
            throw new RuntimeException("父评论不存在");
        }

        Long rootId = (parent.getRootId() == null || parent.getRootId() == 0)
                ? parent.getId()
                : parent.getRootId();
        comment.setRootId(rootId);

        comment.setReplyToUserId(dto.getReplyToUserId());
        comment.setReplyToCommentId(dto.getReplyToCommentId());
    }

    private void updateRootIdIfRootComment(MusicComment comment) {
        if (comment.getParentId() == 0) {
            MusicComment updateEntity = new MusicComment();
            updateEntity.setId(comment.getId());
            updateEntity.setRootId(comment.getId());
            commentMapper.updateById(updateEntity);
        }
    }


    @Override
    public List<CommentVo> getReplies(Long commentId) {
        return commentMapper.listReplies(commentId);
    }

    @Override
    public IPage<AdminCommentPageVO> adminPage(Integer pageNum, Integer pageSize,
                                               String keyword, Long parentId, Integer targetType, Long targetId,
                                               Integer replied, String singerName) {
        Page<AdminCommentPageVO> page = createValidatedPage(pageNum, pageSize);
        IPage<AdminCommentPageVO> comments = baseMapper.pageAdminComments(
                page, keyword, parentId, targetType, targetId, replied, singerName);

        populateRepliesForAdminComments(comments.getRecords());
        return comments;
    }

    private Page<AdminCommentPageVO> createValidatedPage(Integer pageNum, Integer pageSize) {
        int validatedPageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int validatedPageSize = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        return new Page<>(validatedPageNum, validatedPageSize);
    }

    private void populateRepliesForAdminComments(List<AdminCommentPageVO> comments) {
        for (AdminCommentPageVO comment : comments) {
            List<CommentVo> replies = commentMapper.listReplies(comment.getId());
            comment.setReplies(replies);
        }
    }
}
