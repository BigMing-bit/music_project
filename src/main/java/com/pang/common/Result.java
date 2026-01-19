package com.pang.common;

import lombok.Data;

@Data
public class Result {

    private int code;
    private String msg;
    private Object data;
    private long timestamp;

    public static Result success(Object data) {
        Result r = new Result();
        r.code = 0;
        r.msg = "success";
        r.data = data;
        r.timestamp = System.currentTimeMillis();
        return r;
    }

    public static Result successMsg(String msg) {
        Result r = new Result();
        r.code = 0;
        r.msg = msg;
        r.data = null;
        r.timestamp = System.currentTimeMillis();
        return r;
    }

    public static Result success(String msg, Object data) {
        Result r = new Result();
        r.code = 0;
        r.msg = msg;
        r.data = data;
        r.timestamp = System.currentTimeMillis();
        return r;
    }


    public static Result error(int code, String msg) {
        Result r = new Result();
        r.code = code;
        r.msg = msg;
        r.data = null;
        r.timestamp = System.currentTimeMillis();
        return r;
    }
}
