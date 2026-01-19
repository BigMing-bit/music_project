package com.pang.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.pang.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @SaCheckLogin
    @GetMapping("/admin/test")
    public Result test() {
        return Result.success("已登录");
    }
}
