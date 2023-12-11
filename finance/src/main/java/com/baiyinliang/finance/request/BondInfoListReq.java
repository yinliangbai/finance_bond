package com.baiyinliang.finance.request;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode()
@Data
public class BondInfoListReq {

    // 深市、沪市
    private String[] market;
    // 查询条件 转债代码或名称
    private String param;

    private String prop;

    private String sort;

    private Integer pageNum;

    private Integer pageSize;

}
