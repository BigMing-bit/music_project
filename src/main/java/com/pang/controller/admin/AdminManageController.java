package com.pang.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import com.pang.common.Result;
import com.pang.entity.SysAdmin;
import com.pang.entity.SysRole;
import com.pang.mapper.SysRoleMapper;
import com.pang.security.dto.AdminQueryDTO;
import com.pang.security.dto.AdminSaveDTO;
import com.pang.service.AdminService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/admins")
@Api(tags = "管理员管理")
@RequiredArgsConstructor
public class AdminManageController {

    private final AdminService adminService;
    private final SysRoleMapper roleMapper;

    @SaCheckPermission(value = "system:admin:view", type = "admin")
    @GetMapping("/list")
    public Result list(AdminQueryDTO queryDTO) {
        return Result.success(adminService.adminPage(queryDTO));
    }

    @SaCheckPermission(value = "system:admin:view", type = "admin")
    @GetMapping("/{id}")
    public Result info(@PathVariable Long id) {
        return Result.success(adminService.getById(id));
    }

    @SaCheckPermission(value = "system:admin:add", type = "admin")
    @PostMapping
    public Result save(@RequestBody AdminSaveDTO dto) {
        SysAdmin admin = dto.getAdmin();
        if (admin.getId() == null) admin.setCreateTime(LocalDateTime.now());
        adminService.save(admin);
        adminService.saveAdminRoles(admin.getId(), dto.getRoleIds());
        return Result.success("保存成功");
    }

    @SaCheckPermission(value = "system:admin:edit", type = "admin")
    @PutMapping
    public Result update(@RequestBody AdminSaveDTO dto) {
        SysAdmin admin = dto.getAdmin();
        admin.setUpdateTime(LocalDateTime.now());
        adminService.updateById(admin);
        adminService.saveAdminRoles(admin.getId(), dto.getRoleIds());
        return Result.success("更新成功");
    }
    
    @SaCheckPermission(value = "system:admin:view", type = "admin")
    @GetMapping("/{id}/roles")
    public Result getAdminRoles(@PathVariable Long id) {
        return Result.success(adminService.getRolesByAdminId(id));
    }

    @SaCheckPermission(value = "system:admin:edit", type = "admin")
    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        adminService.lambdaUpdate()
                .eq(SysAdmin::getId, id)
                .set(SysAdmin::getStatus, params.get("status"))
                .set(SysAdmin::getUpdateTime, LocalDateTime.now())
                .update();
        return Result.success("状态更新成功");
    }

    @SaCheckPermission(value = "system:admin:delete", type = "admin")
    @DeleteMapping
    public Result delete(@RequestBody List<Long> ids) {
        Assert.isTrue(ArrayUtil.isNotEmpty(ids.toArray()), "请选择删除项");
        adminService.removeBatchByIds(ids);
        return Result.success("删除成功");
    }

    @SaCheckPermission(value = "system:admin:view", type = "admin")
    @GetMapping("/roles")
    public Result getRoles() {
        return Result.success(roleMapper.selectList(null));
    }
}
