package com.pang.controller.admin;

import com.pang.common.Result;
import com.pang.entity.SysAdmin;
import com.pang.security.dto.ChangePasswordDTO;
import com.pang.service.AdminService;
import com.pang.utils.MD5Utils;
import com.pang.utils.SaTokenUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/profile")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }



    @GetMapping
    public Result getProfile() {
        Long adminId = SaTokenUtil.ADMIN.getLoginIdAsLong();
        SysAdmin admin = adminService.getById(adminId);
        admin.setPassword(null); // 不返回密码
        return Result.success(admin);
    }

    // 更新管理员信息（修改昵称/头像）
    @PutMapping
    public Result updateProfile(@RequestBody SysAdmin admin) {
        Long adminId = SaTokenUtil.ADMIN.getLoginIdAsLong();  // 获取当前管理员ID
        admin.setId(adminId);  // 设置要更新的管理员ID

        // ❗这里不要接收 password，避免误改
        admin.setPassword(null);

        // 使用 MyBatis-Plus 的 updateById 进行更新
        boolean updated = adminService.updateById(admin);  // 返回是否更新成功
        if (updated) {
            return Result.success(null);
        }
        return Result.error(500,"更新失败");
    }

    // ✅ 修改密码：必须旧密码 + 新密码
    @PutMapping("/password")
    public Result changePassword(@Validated @RequestBody ChangePasswordDTO dto) {
        Long adminId = SaTokenUtil.ADMIN.getLoginIdAsLong();

        SysAdmin db = adminService.getById(adminId);
        if (db == null) {
            return Result.error(404, "管理员不存在");
        }

        String oldMd5 = MD5Utils.encode(dto.getOldPassword());
        if (!oldMd5.equals(db.getPassword())) {
            return Result.error(400, "旧密码错误");
        }

        SysAdmin update = new SysAdmin();
        update.setId(adminId);
        update.setPassword(MD5Utils.encode(dto.getNewPassword()));
        update.setUpdateTime(LocalDateTime.now());

        boolean ok = adminService.updateById(update);
        if (!ok) return Result.error(500, "修改失败");

        // ✅ 修改密码后：强制当前账号下线
        SaTokenUtil.ADMIN.logout(adminId);

        return Result.success("密码修改成功，请重新登录");
    }
}
