package com.pang.utils;

import cn.dev33.satoken.stp.StpLogic;

public class SaTokenUtil {

    // ✅ 后台管理员体系
    public static final StpLogic ADMIN = new StpLogic("admin");

    // ✅ App用户体系
    public static final StpLogic USER = new StpLogic("user");


    public static Long getUserId() {
        return USER.getLoginIdAsLong();
    }


    public static Long getAdminId() {
        return ADMIN.getLoginIdAsLong();
    }
}
