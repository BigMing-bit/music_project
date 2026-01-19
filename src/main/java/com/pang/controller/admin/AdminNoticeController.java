package com.pang.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.pang.common.Result;
import com.pang.common.log.OpLog;
import com.pang.entity.Notice;
import com.pang.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/notices")
@Api(tags = "公告管理")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticeService noticeService;


    @SaCheckPermission(value = "comments:notice:view", type = "admin")
    @GetMapping
    @ApiOperation("公告分页列表")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(required = false) Integer visible) {
        return Result.success(noticeService.adminPage(pageNum, pageSize, keyword, status, visible));
    }

    @OpLog(module = "公告", action = "新增公告")
    @SaCheckPermission(value = "comments:notice:add", type = "admin")
    @PostMapping
    @ApiOperation("新增公告")
    public Result add(@RequestBody Notice notice) {
        notice.setCreateTime(LocalDateTime.now());
        notice.setUpdateTime(LocalDateTime.now());
        noticeService.save(notice);
        return Result.success("保存成功");
    }

    @SaCheckPermission(value = "comments:notice:edit", type = "admin")
    @PutMapping
    @ApiOperation("更新公告")
    public Result update(@RequestBody Notice notice) {
        notice.setUpdateTime(LocalDateTime.now());
        noticeService.updateById(notice);
        return Result.success("更新成功");
    }

    @OpLog(module = "公告", action = "更新公告状态")
    @SaCheckPermission(value = "comments:notice:edit", type = "admin")
    @PutMapping("/{id}/status")
    @ApiOperation("更新公告状态")
    public Result updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        Notice n = new Notice();
        n.setId(id);
        n.setStatus(body.get("status"));
        n.setUpdateTime(LocalDateTime.now());
        noticeService.updateById(n);
        return Result.success("状态更新成功");
    }

    @SaCheckPermission(value = "comments:notice:delete", type = "admin")
    @DeleteMapping
    @ApiOperation("批量删除公告")
    public Result delete(@RequestBody Long[] ids) {
        noticeService.removeBatchByIds(List.of(ids));
        return Result.success("删除成功");
    }

    @SaCheckPermission(value = "comments:notice:view", type = "admin")
    @GetMapping("/{id}")
    public Result info(@PathVariable Long id) {
        return Result.success(noticeService.adminDetail(id));
    }

}
