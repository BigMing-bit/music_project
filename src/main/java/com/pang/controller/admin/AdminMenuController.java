package com.pang.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pang.common.Result;
import com.pang.entity.SysMenu;
import com.pang.service.MenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/menus")
@Api(tags = "菜单管理")
public class AdminMenuController {

    private final MenuService menuService;

    public AdminMenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @SaCheckPermission(value = "system:menu:view", type = "admin")
    @GetMapping
    @ApiOperation("菜单分页列表")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer status) {

        IPage<SysMenu> page = menuService.adminPage(pageNum, pageSize, keyword, status);
        return Result.success(page);
    }

    @SaCheckLogin(type = "admin")
    @GetMapping("/router-tree")
    public Result routerTree() {
        return Result.success(menuService.getAdminMenuTree());
    }
    /**
     * ✅ 菜单管理页面（只有超级管理员能看）
     */
    @SaCheckPermission(value = "system:menu:view", type = "admin")
    @GetMapping("/tree")
    public Result tree(@RequestParam(required = false) Integer status) {
        return Result.success(menuService.getMenuTree(status));
    }

    @SaCheckPermission(value = "system:menu:view", type = "admin")
    @GetMapping("/perms")
    public Result perms() {
        return Result.success(menuService.getAdminPerms());
    }

    @SaCheckPermission(value = "system:menu:view", type = "admin")
    @GetMapping("/{id}")
    @ApiOperation("菜单详情")
    public Result info(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @SaCheckPermission(value = "system:menu:add", type = "admin")
    @PostMapping
    @ApiOperation("新增菜单")
    public Result add(@RequestBody SysMenu menu) {
        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        menuService.save(menu);
        return Result.success("保存成功");
    }

    @SaCheckPermission(value = "system:menu:edit", type = "admin")
    @PutMapping
    @ApiOperation("更新菜单")
    public Result update(@RequestBody SysMenu menu) {
        menu.setUpdateTime(LocalDateTime.now());
        menuService.updateById(menu);
        return Result.success("更新成功");
    }

    @SaCheckPermission(value = "system:menu:edit", type = "admin")
    @PutMapping("/{id}/status")
    @ApiOperation("更新菜单状态")
    public Result updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        SysMenu m = new SysMenu();
        m.setId(id);
        m.setStatus(body.get("status"));
        m.setUpdateTime(LocalDateTime.now());
        menuService.updateById(m);
        return Result.success("状态更新成功");
    }

    @SaCheckPermission(value = "system:menu:delete", type = "admin")
    @DeleteMapping("/{id}")
    @ApiOperation("删除菜单")
    public Result delete(@PathVariable Long id) {
        menuService.removeById(id);
        return Result.success("删除成功");
    }

    @SaCheckPermission(value = "system:menu:delete", type = "admin")
    @DeleteMapping
    @ApiOperation("批量删除菜单")
    public Result delete(@RequestBody Long[] ids) {
        menuService.removeBatchByIds(List.of(ids));
        return Result.success("删除成功");
    }



}
