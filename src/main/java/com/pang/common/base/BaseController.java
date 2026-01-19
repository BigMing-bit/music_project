package com.pang.common.base;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.ServletRequestUtils;

import java.util.Map;

public class BaseController {

    @Resource
    HttpServletRequest req;

    public <T> Page<T> getPage() {
        int current = ServletRequestUtils.getIntParameter(req, "current", 1);
        int size = ServletRequestUtils.getIntParameter(req, "size", 10);
        return PageDTO.of(current, size);
    }

    public <T> QueryWrapper<T> queryLike(T entity) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        Map<String, Object> params = BeanUtil.beanToMap(entity, false, true);
        params.forEach(wrapper::like);
        return wrapper;
    }
}
