package com.baiyinliang.finance.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author a
 */
@Slf4j
public class EnumSupport {

    @Nullable
    public static <K, V> Object get(Map<K, V> map, K key, String fieldName) {
        if (key == null) {
            log.warn("参数为空");
        } else if (map.containsKey(key)) {
            return doGet(map, key, fieldName);
        }
        return null;
    }

    @NonNull
    public static <K, V, T> T getOrDefault(Map<K, V> map, K key, String fieldName, T defaultResult) {
        if (key == null) {
            log.warn("参数为空");
        } else if (map.containsKey(key)) {
            Object obj;
            if ((obj = doGet(map, key, fieldName)) != null) {
                return (T)obj;
            }
        }
        return defaultResult;
    }

    @Nullable
    public static <K, V, X extends Throwable> Object get(Map<K, V> map, K key, String fieldName, Supplier<? extends X> exceptionSupplier) throws Throwable {
        if (key == null) {
            throw new NullPointerException();
        }
        if (map.containsKey(key)) {
            return doGet(map, key, fieldName);
        }

        throw exceptionSupplier.get();
    }


    @Nullable
    private static <K, V> Object doGet(Map<K, V> map, K key, String fieldName) {
        V v = map.get(key);
        try {
            Field f = v.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(v);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }


}
