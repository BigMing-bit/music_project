package com.pang.controller.admin;

import com.pang.common.Result;
import com.pang.service.SingerRankService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/rank/singer")
public class AdminSingerRankController {

    private final SingerRankService singerRankService;

    public AdminSingerRankController(SingerRankService singerRankService) {
        this.singerRankService = singerRankService;
    }

    @PostMapping("/generate")
    public Result generate() {
        singerRankService.generateAllTimeRank();
        return Result.success("生成成功");
    }
}
