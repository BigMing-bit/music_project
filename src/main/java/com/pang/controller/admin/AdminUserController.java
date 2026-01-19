package com.pang.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import com.pang.common.Result;
import com.pang.service.UserService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/admin/users")
@Api(tags = "用户管理")
@RequiredArgsConstructor
public class AdminUserController {


    private final UserService userService;

    @SaCheckPermission(value = "system:user:view", type = "admin")
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(required = false) Integer gender) {
        return Result.success(userService.adminPage(pageNum, pageSize, keyword, status,gender));
    }

    @SaCheckPermission(value = "system:user:view", type = "admin")
    @GetMapping("/{id}")
    public Result info(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }


    @SaCheckPermission(value = "system:user:delete", type = "admin")
    @DeleteMapping
    public Result delete(@RequestBody Long[] ids) {
        Assert.isTrue(ArrayUtil.isNotEmpty(ids), "请选择删除项");
        userService.removeBatchByIds(List.of(ids));
        return Result.success("删除成功");
    }
}
