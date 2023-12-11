package com.baiyinliang.finance.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

public class CommonEnums {

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static enum Direction {
        ASC("asc"), DESC("desc");
        private String code;
    }
}
