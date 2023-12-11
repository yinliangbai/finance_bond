package com.baiyinliang.finance.service;

public interface YangKeDuoService {

    void sendCode();

    String submitLogin(String code);

    void visitYangKeDuo(int offset, int limit, String access_token);

}
