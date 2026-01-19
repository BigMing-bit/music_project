package com.pang.controller.app;

import com.pang.common.Result;
import com.pang.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notices")
@Api(tags = "前台公告")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping("/latest")
    @ApiOperation("前台公告列表（最新）")
    public Result latest(@RequestParam(defaultValue = "10") Integer size) {
        return Result.success(noticeService.frontLatest(size));
    }

    @GetMapping("/{id}")
    @ApiOperation("前台公告详情")
    public Result detail(@PathVariable Long id) {
        return Result.success(noticeService.frontDetail(id));
    }
}
