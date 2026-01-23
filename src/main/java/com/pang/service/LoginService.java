package com.pang.service;

import com.pang.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface LoginService<T, DTO> {


    Result login(DTO dto, HttpServletRequest request);

    Result logout();
    

    Map<String, Object> buildLoginResponseData(T user);
}
