package com.pang.controller.app;

import com.pang.common.Result;
import com.pang.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public Result list() {
        return Result.success(tagService.listEnabled());
    }

    @GetMapping("/group")
    public Result group() {
        return Result.success(tagService.groupEnabled());
    }
}
