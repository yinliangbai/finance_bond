package com.baiyinliang.finance.response;


import com.baiyinliang.finance.entity.BondRate;

public enum ResultEnums {

    SUCCESS("200", "请求成功"),
    ERROR("-1", "请求失败"),
    SYSTEM_ERROR("500", "系统异常"),
    ;

    private String code;
    private String msg;


    ResultEnums(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}