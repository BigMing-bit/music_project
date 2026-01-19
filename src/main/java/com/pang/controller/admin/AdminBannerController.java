package com.pang.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pang.common.Result;
import com.pang.entity.Banner;
import com.pang.service.BannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/banners")
@Api(tags = "轮播图管理")
public class AdminBannerController {

    private final BannerService bannerService;

    public AdminBannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @SaCheckPermission(value = "comments:banner:view", type = "admin")
    @GetMapping
    @ApiOperation("轮播图分页列表")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer status) {

        IPage<Banner> page = bannerService.adminPage(pageNum, pageSize, keyword, status);
        return Result.success(page);
    }

    @SaCheckPermission(value = "comments:banner:view", type = "admin")
    @GetMapping("/{id}")
    @ApiOperation("轮播图详情")
    public Result info(@PathVariable Long id) {
        return Result.success(bannerService.getById(id));
    }

    @SaCheckPermission(value = "comments:banner:add", type = "admin")
    @PostMapping
    @ApiOperation("新增轮播图")
    public Result add(@RequestBody Banner banner) {
        banner.setCreateTime(LocalDateTime.now());
        banner.setUpdateTime(LocalDateTime.now());
        bannerService.save(banner);
        return Result.success("保存成功");
    }

    @SaCheckPermission(value = "comments:banner:edit", type = "admin")
    @PutMapping
    @ApiOperation("更新轮播图")
    public Result update(@RequestBody Banner banner) {
        banner.setUpdateTime(LocalDateTime.now());
        bannerService.updateById(banner);
        return Result.success("更新成功");
    }

    @SaCheckPermission(value = "comments:banner:edit", type = "admin")
    @PutMapping("/{id}/status")
    @ApiOperation("更新轮播图状态")
    public Result updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        Banner b = new Banner();
        b.setId(id);
        b.setStatus(body.get("status"));
        b.setUpdateTime(LocalDateTime.now());
        bannerService.updateById(b);
        return Result.success("状态更新成功");
    }

    @SaCheckPermission(value = "comments:banner:delete", type = "admin")
    @DeleteMapping
    @ApiOperation("批量删除轮播图")
    public Result delete(@RequestBody Long[] ids) {
        bannerService.removeBatchByIds(List.of(ids));
        return Result.success("删除成功");
    }
}
