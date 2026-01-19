package com.pang.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.pang.common.Result;
import com.pang.service.MusicCommentService;
import com.pang.utils.SaTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final MusicCommentService commentService;

    @SaCheckPermission(value = "comments:comment:view", type = "admin")
    @GetMapping
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long parentId,
                       @RequestParam(required = false) Integer targetType,   // 修改为 targetType 和 targetId
                       @RequestParam(required = false) Long targetId,
                       @RequestParam(required = false) Integer replied,
                       @RequestParam(required = false) String singerName) {

        return Result.success(commentService.adminPage(pageNum, pageSize, keyword, parentId, targetType, targetId, replied, singerName));
    }

    @SaCheckPermission(value = "comments:comment:view", type = "admin")
    @GetMapping("/{id}")
    public Result info(@PathVariable Long id) {
        return Result.success(commentService.getById(id));
    }

    @SaCheckPermission(value = "comments:comment:reply", type = "admin")
    @PostMapping("/{id}/reply")
    public Result reply(@PathVariable Long id,
                        @RequestBody Map<String, String> body) {

        Long adminId = SaTokenUtil.ADMIN.getLoginIdAsLong();
        String replyContent = body.get("replyContent");

        Object dn = SaTokenUtil.ADMIN.getSession().get("displayName");
        String adminName = dn == null ? null : dn.toString();

        commentService.reply(id, replyContent, adminId, adminName);
        return Result.success("回复成功");
    }

    @SaCheckPermission(value = "comments:comment:delete", type = "admin")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return commentService.removeById(id) ? Result.success("删除成功") : Result.error(400, "删除失败");
    }

    @SaCheckPermission(value = "comments:comment:delete", type = "admin")
    @DeleteMapping
    public Result deleteBatch(@RequestBody List<Long> ids) {
        return commentService.removeBatchByIdsSafe(ids) ? Result.success("删除成功") : Result.error(400, "删除失败");
    }
}

