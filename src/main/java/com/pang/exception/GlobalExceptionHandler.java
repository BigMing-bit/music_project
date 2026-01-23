package com.pang.exception;

import com.pang.common.constants.CommonConstants;
import com.pang.common.Result;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result handleBizException(BizException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(GlobalException.class)
    public Result handleGlobalException(GlobalException e) {
        return Result.error(CommonConstants.FAIL, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        return Result.error(CommonConstants.FAIL, "服务器内部错误: " + e.getMessage());
    }
}

