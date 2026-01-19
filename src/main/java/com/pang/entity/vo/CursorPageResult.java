package com.pang.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class CursorPageResult<T> {
    private List<T> list;
    private String nextCursor;
    private Boolean hasMore;
}
