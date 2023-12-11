package com.baiyinliang.finance.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ModuleEnum {


    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum BusinessSituation {
        INITIAL("INITIAL", "初始状态"),
        DEVELOPMENT("DEVELOPMENT", "发展状态"),
        ;

        private String code;
        private String name;

        // static Map<Object, Object> map = initMap(BusinessSituation.class, "code");
    }

    class C{
        void aa() {
            System.out.println("true = " + true);
        }
    }



    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum LifeState{
        BORN(0, "出生"),
        PRESCHOOL(1, "学前班"),
        ;

        private int code;
        private String name;

        // initMap(LifeState.class, "code");
        public static Map<Object, Object> map = initMap(LifeState.class, "code");
        // String getName(String code){
        //     for (LifeState value : LifeState.values()) {
        //
        //     }
        // }
    }


    static Map<Object, Object> initMap(Class<?> clazz, String filedName) {
        Map<Object, Object> map = new HashMap<>();
        if (clazz.isEnum()) {
            Object[] enumConstants = clazz.getEnumConstants();
            try {
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
