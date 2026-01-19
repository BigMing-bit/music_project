package com.pang.utils;

import org.springframework.util.DigestUtils;

public class MD5Utils {
    public static String encode(String str) {
        return DigestUtils.md5DigestAsHex(str.getBytes());
    }
}
