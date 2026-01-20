package com.pang.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.spring.SpringMVCUtil;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.pang.utils.SaTokenUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Sa-Token 配置类
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // ✅ 1) 后台 admin 拦截器
        registry.addInterceptor(new SaInterceptor(handler -> {

                    // ✅ OPTIONS 放行
                    String method = SpringMVCUtil.getRequest().getMethod();
                    if ("OPTIONS".equalsIgnoreCase(method)) {
                        return;
                    }

                    // ✅ 关键：用 admin 登录体系校验
                    SaTokenUtil.ADMIN.checkLogin();

                }))
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login");

        // ✅ 2) App 端拦截器
        registry.addInterceptor(new SaInterceptor(handler -> {

                    String method = SpringMVCUtil.getRequest().getMethod();
                    if ("OPTIONS".equalsIgnoreCase(method)) {
                        return;
                    }

                    SaTokenUtil.USER.checkLogin();

                }))
                .addPathPatterns("/app/**")
                .excludePathPatterns(
                        "/app/user/login",
                        "/app/user/register",
                        "/app/user/captcha",
                        "/app/user/send-sms",
                        "/app/user/info/**",
                        "/app/user/stats/**",
                        "/app/user/playlists/**",
                        "/app/playlists/**"
                );

        // ✅ 文件上传 /file/** 需要用户登录（支持 admin 和 user 两种登录体系）
        registry.addInterceptor(new SaInterceptor(handler -> {

                    String method = SpringMVCUtil.getRequest().getMethod();
                    if ("OPTIONS".equalsIgnoreCase(method)) {
                        return;
                    }

                    // 检查是否已经登录（优先检查 admin，再检查 user）
                    if (!SaTokenUtil.ADMIN.isLogin() && !SaTokenUtil.USER.isLogin()) {
                        // 两个体系都未登录，抛出未登录异常
                        SaTokenUtil.USER.checkLogin();
                    }

                }))
                .addPathPatterns("/file/**");
    }

}

