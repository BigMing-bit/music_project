package com.pang.controller.app;

import com.pang.common.Result;
import com.pang.security.dto.CommentCreateDTO;
import com.pang.service.MusicCommentService;

import com.pang.utils.SaTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/comments")
@RequiredArgsConstructor
public class CommentController {

    private final MusicCommentService commentService;

    @GetMapping
    public Result list(@RequestParam Integer targetType,
                       @RequestParam Long targetId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(commentService.pageComments(targetType, targetId, page, pageSize));
    }

    @GetMapping("/replies")
    public Result getReplies(@RequestParam Long commentId) {
        return Result.success(commentService.getReplies(commentId));  // 获取回复
    }

    @PostMapping
    public Result create(@RequestBody CommentCreateDTO dto) {
        SaTokenUtil.USER.checkLogin();
        Long userId = SaTokenUtil.USER.getLoginIdAsLong();
        return Result.success(commentService.create(dto, userId));
    }
}

