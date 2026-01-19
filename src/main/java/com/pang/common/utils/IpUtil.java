package com.pang.common.utils;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtil {

    private static final String UNKNOWN = "unknown";

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_X_REAL_IP = "X-Real-IP";

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";

        // 1) 先尝试 X-Forwarded-For（可能是多个 IP：client, proxy1, proxy2）
        String ip = getHeader(request, HEADER_X_FORWARDED_FOR);
        if (!isInvalid(ip)) {
            // 取第一个非 unknown 的
            String[] parts = ip.split(",");
            for (String p : parts) {
                String candidate = p.trim();
                if (!isInvalid(candidate)) {
                    ip = candidate;
                    break;
                }
            }
        } else {
            // 2) 再尝试 X-Real-IP
            ip = getHeader(request, HEADER_X_REAL_IP);
        }

        // 3) 最后退回 remoteAddr
        if (isInvalid(ip)) {
            ip = request.getRemoteAddr();
        }

        // ✅ 开发期：统一本机回环地址为 127.0.0.1
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }

        // ✅ 处理 IPv6-mapped IPv4： ::ffff:127.0.0.1
        if (ip != null && ip.startsWith("::ffff:")) {
            ip = ip.substring(7);
        }

        return ip;
    }

    private static String getHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return (value != null && !value.isBlank()) ? value : null;
    }

    private static boolean isInvalid(String ip) {
        return ip == null || ip.isBlank() || UNKNOWN.equalsIgnoreCase(ip);
    }
}
