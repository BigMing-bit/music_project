package com.pang.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pang.entity.MusicComment;
import com.pang.entity.vo.AdminCommentPageVO;
import com.pang.entity.vo.CommentVo;
import com.pang.mapper.MusicCommentMapper;
import com.pang.mapper.SysAdminMapper;
import com.pang.security.dto.CommentCreateDTO;
import com.pang.service.MusicCommentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MusicCommentServiceImpl extends ServiceImpl<MusicCommentMapper, MusicComment> implements MusicCommentService {

    @Resource
    private SysAdminMapper sysAdminMapper;

    @Resource
    private MusicCommentMapper commentMapper;

    @Override
    @Transactional
    public void reply(Long commentId, String replyContent, Long adminId, String adminName) {

        if (commentId == null) throw new RuntimeException("commentId不能为空");
        if (replyContent == null || replyContent.isBlank()) throw new RuntimeException("回复内容不能为空");

        // ✅ 正经兜底逻辑
        if (adminName == null || adminName.isBlank()) {
            adminName = sysAdminMapper.selectDisplayName(adminId);
        }
        if (adminName == null || adminName.isBlank()) {
            adminName = "Harmony 官方"; // 或 “官方”
        }

        int n = baseMapper.replyComment(commentId, replyContent.trim(), adminId, adminName);
        if (n <= 0) throw new RuntimeException("回复失败：评论不存在");
    }
    /**
     * ✅ 批量删除：主评论 + 子评论（用户回复用户/管理员回复等）
     */
    @Override
    @Transactional
    public boolean removeBatchByIdsSafe(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return true;

        for (Long id : ids) {
            if (id != null) {
                baseMapper.deleteCommentAndChildren(id);
            }
        }
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
    @Transactional
    public Long create(CommentCreateDTO dto, Long userId) {
        if (dto.getTargetType() == null || dto.getTargetId() == null) {
            throw new RuntimeException("targetType/targetId不能为空");
        }
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new RuntimeException("内容不能为空");
        }

        MusicComment c = new MusicComment();
        c.setTargetType(dto.getTargetType());
        c.setTargetId(dto.getTargetId());
        c.setUserId(userId);
        c.setContent(dto.getContent().trim());
        c.setStatus(1);
        c.setLikeCount(0);
        c.setCreatorType(1); // 用户评论

        Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
        c.setParentId(parentId);

        // 如果是根评论
        if (parentId == 0) {
            c.setRootId(0L);
        } else {
            // 如果是回复，获取父评论并设置 rootId
            MusicComment parent = commentMapper.selectById(parentId);
            if (parent == null) throw new RuntimeException("父评论不存在");
            Long rootId = (parent.getRootId() == null || parent.getRootId() == 0) ? parent.getId() : parent.getRootId();
            c.setRootId(rootId);

            c.setReplyToUserId(dto.getReplyToUserId());
            c.setReplyToCommentId(dto.getReplyToCommentId());
        }

        commentMapper.insert(c);

        if (parentId == 0) {
            MusicComment upd = new MusicComment();
            upd.setId(c.getId());
            upd.setRootId(c.getId());
            commentMapper.updateById(upd);
        }

        return c.getId();
    }

    @Override
    public List<CommentVo> getReplies(Long commentId) {
        return commentMapper.listReplies(commentId);  // 从数据库获取回复
    }

    @Override
    public IPage<AdminCommentPageVO> adminPage(Integer pageNum, Integer pageSize,
                                               String keyword, Long parentId, Integer targetType, Long targetId,
                                               Integer replied, String singerName) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;

        Page<AdminCommentPageVO> page = new Page<>(pageNum, pageSize);
        IPage<AdminCommentPageVO> comments = baseMapper.pageAdminComments(page, keyword, parentId, targetType, targetId, replied, singerName);

        // 获取每个主评论的回复
        for (AdminCommentPageVO comment : comments.getRecords()) {
            List<CommentVo> replies = commentMapper.listReplies(comment.getId());  // 获取子评论
            comment.setReplies(replies); // 设置子评论到主评论中
        }

        return comments;
    }

}
