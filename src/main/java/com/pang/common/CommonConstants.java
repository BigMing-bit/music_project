package com.pang.common;

public class CommonConstants {

    private CommonConstants() {}

    // Result code（你 Result.success 现在用 1 也行，但建议成功=0）
    public static final int SUCCESS = 0;
    public static final int FAIL = 500;

    // 业务错误码（写你自己喜欢的）
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;

    // 业务：账号相关
    public static final int USERNAME_EXISTS = 41001;
    public static final int LOGIN_FAILED = 41002;
    public static final int ACCOUNT_DISABLED = 41003;
}
