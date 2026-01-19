package com.pang.common.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pang.entity.SysAdmin;
import com.pang.entity.SysOperationLog;
import com.pang.service.AdminService;
import com.pang.service.SysOperationLogService;
import com.pang.utils.SaTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
public class OpLogAspect {

    private final SysOperationLogService logService;
    private final AdminService adminService;
    private final HttpServletRequest request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(com.pang.common.log.OpLog)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        OpLog opLog = method.getAnnotation(OpLog.class);

        SysOperationLog log = new SysOperationLog();
        log.setModule(opLog.module());
        log.setAction(opLog.action());
        log.setMethod(request.getMethod());
        log.setPath(request.getRequestURI());
        log.setIp(getClientIp(request));
        log.setCreateTime(LocalDateTime.now());

        // ✅ 管理员信息（未登录则不记录 adminId）
        try {
            if (SaTokenUtil.ADMIN.isLogin()) {
                Long adminId = SaTokenUtil.ADMIN.getLoginIdAsLong();
                log.setAdminId(adminId);

                SysAdmin admin = adminService.getById(adminId);
                log.setAdminUsername(admin != null ? admin.getUsername() : "");
            }
        } catch (Exception ignored) {}

        // ✅ 请求参数（避免巨大对象：只记 request.getParameterMap / 或 joinPoint args）
        try {
            // 记录参数（更稳：记录 query/form 参数；json body 会为空）
            String paramsJson = objectMapper.writeValueAsString(request.getParameterMap());
            log.setParams(paramsJson);
        } catch (Exception ignored) {}

        try {
            Object result = pjp.proceed();
            log.setSuccess(1);
            logService.save(log);
            return result;
        } catch (Throwable ex) {
            log.setSuccess(0);
            log.setErrorMsg(trimErr(ex.getMessage()));
            logService.save(log);
            throw ex;
        }
    }

    private String trimErr(String msg) {
        if (msg == null) return null;
        return msg.length() > 480 ? msg.substring(0, 480) : msg;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) return ip.split(",")[0].trim();
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) return ip.trim();
        return request.getRemoteAddr();
    }
}
