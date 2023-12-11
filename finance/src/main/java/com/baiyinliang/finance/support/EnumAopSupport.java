package com.baiyinliang.finance.support;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author a
 */
public interface EnumAopSupport {

    default Map<Object, Object> initMap(Class<?> clazz, String filedName) {
        Map<Object, Object> map = new HashMap<>();
        if (clazz.isEnum()) {
            try {
                Object[] enumConstants = clazz.getEnumConstants();
                Field field = clazz.getDeclaredField(filedName);
                field.setAccessible(true);
                for (Object enumConstant : enumConstants) {
                    map.put(field.get(enumConstant), enumConstant);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return map;
    }
}
