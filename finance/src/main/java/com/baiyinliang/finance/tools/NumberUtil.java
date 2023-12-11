package com.baiyinliang.finance.tools;

import com.baiyinliang.finance.common.Constants;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class NumberUtil {

    public static BigDecimal subRemainder(Integer sourceNumber) {
        return subRemainder(sourceNumber, 2);
    }


    /**
     * 保留N位小数（不四舍五入）
     *
     * @param sourceNumber 待处理的数值
     * @param scale        要保留的小数位数
     * @return 保留小数位后的数值
     * @description 舍弃多余的小数位
     */
    public static BigDecimal subRemainder(Integer sourceNumber, int scale) {
        if (sourceNumber == null) return BigDecimal.ZERO;
        log.debug("保留小数前：" + sourceNumber);

        BigDecimal number = BigDecimal.valueOf(sourceNumber).divide(Constants.DECIMAL_1000, scale, BigDecimal.ROUND_HALF_UP);// 0.28


        // String转Double
        // 最后一位小数是0的话，会被舍弃
        // 所以，要视实际需要决定返回BigDecimal还是Double类型
        return number;
    }
}
