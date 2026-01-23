package com.pang.controller.app;

import com.pang.common.Result;
import com.pang.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public Result search(@RequestParam String keyword,
                         @RequestParam(defaultValue = "all") String type,
                         @RequestParam(required = false) String cursor,
                         @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(searchService.search(keyword, type, cursor, size));
    }
}
