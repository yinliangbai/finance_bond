package com.baiyinliang.finance.response;

import lombok.Data;

import java.io.Serializable;
@Data
public class ResponseData<T> implements Serializable {

    private static final long serialVersionUID = 3752416489957520969L;

    private static final int DEFAULT_SUCCESS_CODE = 200;
    private static final String DEFAULT_SUCCESS_MESSAGE = "请求成功";
    private static final int DEFAULT_ERROR_CODE = 500;
    private static final String DEFAULT_ERROR_MESSAGE = "请求失败";

    private int code;

    private String msg;

    private T data;
    /**
     * 请求是否成功
     */
    private Boolean success;

    public ResponseData(Boolean success, int code, String msg, T data) {
        this.success = success;
        this.code = code;
        this.msg = msg;
        this.data = data;

    }

    public static <T> ResponseData<T> success() {
        return new ResponseData<>(true, DEFAULT_SUCCESS_CODE, DEFAULT_SUCCESS_MESSAGE, null);
    }


    public static <T> ResponseData<T> success(T object) {
        return new ResponseData<>(true, DEFAULT_SUCCESS_CODE, DEFAULT_SUCCESS_MESSAGE, object);
    }

    public static <T> ResponseData<T> success(int code, String msg, T object) {
        return new ResponseData<>(true, code, msg, object);
    }

    public static <T> ResponseData<T> error(String msg) {
        return new ResponseData<>(false, DEFAULT_ERROR_CODE, msg, null);
    }

    public static <T> ResponseData<T> error(int code, String msg) {
        return new ResponseData<>(false, code, msg, null);
    }

    public static <T> ResponseData<T> error(int code, String msg, T object) {
        return new ResponseData<>(false, code, msg, object);
    }
}