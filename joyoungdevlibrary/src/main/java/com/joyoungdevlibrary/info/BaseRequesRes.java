package com.joyoungdevlibrary.info;

/**
 * Created by Joyoung on 2016/5/23.
 */
public class BaseRequesRes<T> {
    private int code;
    private T data;
    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
