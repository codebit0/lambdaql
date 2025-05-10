package org.lambdaql.utils;

public class PrimitiveHelper {

    public static boolean isBoolean(Class<?> type) {
        return type == boolean.class || type == Boolean.class;
    }

    public static boolean isInt(Class<?> type) {
        return type == int.class || type == Integer.class;
    }
}
