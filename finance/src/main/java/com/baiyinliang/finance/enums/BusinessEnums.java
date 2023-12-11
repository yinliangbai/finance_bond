package com.baiyinliang.finance.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import static com.baiyinliang.finance.common.Constants.*;

public class BusinessEnums {

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static enum PaidFlagEnum {
        NON_PAYMENT(0, "本期未付"),
        CURRENT(1, "本期付款日"),
        PAID(2, "本期已付"),
        ;

        @Getter
        private int code;
        @Getter
        private String name;

        // initMap(LifeState.class, "code");
//        public static Map<Object, Object> map = initMap(ModuleEnum.LifeState.class, "code");
        // String getName(String code){
        //     for (LifeState value : LifeState.values()) {
        //
        //     }
        // }
    }


    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static enum BondMarketEnum {
        SH_MARKET("sh", Arrays.asList(BOND_CODE_PREFIX_110, BOND_CODE_PREFIX_111, BOND_CODE_PREFIX_113, BOND_CODE_PREFIX_118)),
        SZ_MARKET("sz", Arrays.asList(BOND_CODE_PREFIX_123, BOND_CODE_PREFIX_127, BOND_CODE_PREFIX_128)),
        ;
        @Getter
        private String market;
        @Getter
        private List<String> codeList;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static enum DefaultBondCode {
        SZ_128(BOND_CODE_PREFIX_128, 144),
        SZ_127(BOND_CODE_PREFIX_127, 97),
        SZ_123(BOND_CODE_PREFIX_123, 229),
        SH_110(BOND_CODE_PREFIX_110, 95),
        SH_111(BOND_CODE_PREFIX_111, 17),
        SH_113(BOND_CODE_PREFIX_113, 679),
        SH_118(BOND_CODE_PREFIX_118, 45),
        ;

        @Getter
        private String prefix;
        @Getter
        private int code;

        public static int getCodeByPrefix(String prefix) {
            if (StringUtils.isBlank(prefix)) {
                return 0;
            }

            for (DefaultBondCode value : DefaultBondCode.values()) {
                if (value.getPrefix().equals(prefix)) {
                    return value.getCode();
                }
            }

            return 0;
        }
    }


    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static enum BondCodeFlag {
        未上市("未上市", 0),
        上市("上市", 1),
        停牌("停牌", 2),
        退市("退市", 3),
        ;

        @Getter
        private String name;
        @Getter
        private int flag;

    }
}
