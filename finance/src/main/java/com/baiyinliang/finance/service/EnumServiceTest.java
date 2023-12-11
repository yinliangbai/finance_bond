package com.baiyinliang.finance.service;

import com.baiyinliang.finance.enums.ModuleEnum.LifeState;
import com.baiyinliang.finance.support.EnumSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

// @Service
@Slf4j
public class EnumServiceTest {

    public static void main(String[] args) {
        a();
    }

    public static void a() {
        // Object o = ModuleEnum.get(LifeState.map, 1, "name", StringUtils.EMPTY);
        // log.info("o=" + JSON.toJSONString(o));
        System.out.println("LifeState.map = " + LifeState.map);
        System.out.println("LifeState.values() = " + LifeState.values());
        System.out.println("LifeState.valueOf(\"BORN\") = " + LifeState.valueOf("BORN"));
        String name = EnumSupport.getOrDefault(LifeState.map, 1, "name", StringUtils.EMPTY);
        log.info("name=" + name);
        for (LifeState value : LifeState.values()) {
            System.out.println("value = " + value);
        }
    }

}
