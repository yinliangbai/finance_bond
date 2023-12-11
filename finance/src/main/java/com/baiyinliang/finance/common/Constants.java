package com.baiyinliang.finance.common;

import java.math.BigDecimal;

public class Constants {

    // 128开头的深债的当前最大编码为144
    public static final int DEFAULT_BOND_CODE_MAX_128 = 144;

    //"110","111","113","118"
    public static final String BOND_CODE_PREFIX_110 = "110";
    public static final String BOND_CODE_PREFIX_111 = "111";
    public static final String BOND_CODE_PREFIX_113 = "113";
    public static final String BOND_CODE_PREFIX_118 = "118";
    public static final String BOND_CODE_PREFIX_123 = "123";
    public static final String BOND_CODE_PREFIX_127 = "127";
    public static final String BOND_CODE_PREFIX_128 = "128";

    public static final BigDecimal DECIMAL_1000 = new BigDecimal(1000);
    public static final BigDecimal DECIMAL_100 = new BigDecimal(100);
}
