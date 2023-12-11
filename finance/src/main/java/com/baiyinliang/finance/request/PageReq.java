package com.baiyinliang.finance.request;

import lombok.Data;

@Data
public class PageReq {
    private int pageNo = 1;
    private int size = 10;

    private String sort;

    private String property;

}
