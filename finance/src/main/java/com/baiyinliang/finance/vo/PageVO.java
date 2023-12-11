package com.baiyinliang.finance.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageVO<T> {

    private Integer pageNum;

    private Integer pageSize;
    private Integer total;

    private List<T> data;
    private boolean hasNext;


}
