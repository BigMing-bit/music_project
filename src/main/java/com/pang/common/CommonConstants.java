package com.pang.common;

public class CommonConstants {

    private CommonConstants() {}

    // 通用的返回码
    public static final int SUCCESS = 0;  // 成功
    public static final int FAIL = 500;   // 失败

    // 业务错误码
    public static final int BAD_REQUEST = 400;       // 错误的请求
    public static final int UNAUTHORIZED = 401;      // 未授权（未登录或者token失效）
    public static final int FORBIDDEN = 403;         // 禁止访问（无权限）
    public static final int NOT_FOUND = 404;         // 资源未找到

    // 账号相关错误码
    public static final int USERNAME_EXISTS = 41001;  // 用户名已存在
    public static final int LOGIN_FAILED = 41002;     // 登录失败
    public static final int ACCOUNT_DISABLED = 41003; // 账户已禁用
    public static final int PASSWORD_INCORRECT = 41004; // 密码错误

    // 权限相关错误码
    public static final int PERMISSION_DENIED = 42001; // 权限不足

    // 参数相关错误码
    public static final int INVALID_PARAMETER = 43001; // 无效的参数
}
