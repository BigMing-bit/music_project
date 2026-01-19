package com.pang.service;

import com.pang.entity.vo.CursorPageResult;

public interface SearchService {
    CursorPageResult<?> search(String keyword, String type, String cursor, Integer size);
}
