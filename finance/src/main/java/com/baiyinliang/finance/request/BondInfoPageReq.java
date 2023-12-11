package com.baiyinliang.finance.request;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BondInfoPageReq extends Sorter {
    private static final long serialVersionUID = -2865411243426073750L;

    private int pageNo = 1;
    private int size = 10;

    /*protected BondInfoPageReq(int page, int size, Sort sort) {
        super(page, size, sort);
    }

    public BondInfoPageReq(int page, int size, Sort sort, String param) {
        super(page, size, sort);
        this.setParam(param);
    }

    @Getter
    @Setter*/

    //

    // 查询条件
    private String param;
}
