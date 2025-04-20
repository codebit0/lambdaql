package org.lambdaql.function;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Getter
public class JpqlFunction {

    public enum FunctionType {
        STRING,
        NUMERIC,
        DATETIME,
        COLLECTION,
        OPERATOR,
        OTHER
    }


    private static final Map<FunctionType, List<JpqlFunction>> FUNCTION_MAP = new EnumMap<>(FunctionType.class);

    static {
        try {
            // 문자열 관련 JPQL 함수
            // STRING functions
            registerFunction(FunctionType.STRING, "CONCAT", "CONCAT(%s, %s)",
                    new FunctionDescriptor(String.class.getMethod("concat", String.class), 0, 1));
            registerFunction(FunctionType.STRING, "LENGTH", "LENGTH(%s)",
                    new FunctionDescriptor(String.class.getMethod("length"), 0));
            registerFunction(FunctionType.STRING, "LOWER", "LOWER(%s)",
                    new FunctionDescriptor(String.class.getMethod("toLowerCase"), 0));
            registerFunction(FunctionType.STRING, "UPPER", "UPPER(%s)",
                    new FunctionDescriptor(String.class.getMethod("toUpperCase"), 0));
            registerFunction(FunctionType.STRING, "LOCATE", "LOCATE(%s, %s)",
                    new FunctionDescriptor(String.class.getMethod("indexOf", String.class), 0, 1));

            // NUMERIC functions
            registerFunction(FunctionType.NUMERIC, "ABS", "ABS(%s)",
                    new FunctionDescriptor(Math.class.getMethod("abs", int.class), 1));
            registerFunction(FunctionType.NUMERIC, "SQRT", "SQRT(%s)",
                    new FunctionDescriptor(Math.class.getMethod("sqrt", double.class), 1));
            registerFunction(FunctionType.NUMERIC, "MOD", "MOD(%s, %s)",
                    new FunctionDescriptor(Math.class.getMethod("floorMod", int.class, int.class), 1, 2));
            registerFunction(FunctionType.NUMERIC, "POWER", "POWER(%s, %s)",
                    new FunctionDescriptor(Math.class.getMethod("pow", double.class, double.class), 1, 2));
            registerFunction(FunctionType.NUMERIC, "ROUND", "ROUND(%s)",
                    new FunctionDescriptor(Math.class.getMethod("round", double.class), 1));
            registerFunction(FunctionType.NUMERIC, "FLOOR", "FLOOR(%s)",
                    new FunctionDescriptor(Math.class.getMethod("floor", double.class), 1));
            registerFunction(FunctionType.NUMERIC, "CEIL", "CEIL(%s)",
                    new FunctionDescriptor(Math.class.getMethod("ceil", double.class), 1));
            registerFunction(FunctionType.NUMERIC, "EXP", "EXP(%s)",
                    new FunctionDescriptor(Math.class.getMethod("exp", double.class), 1));
            registerFunction(FunctionType.NUMERIC, "LOG", "LOG(%s)",
                    new FunctionDescriptor(Math.class.getMethod("log", double.class), 1));
            registerFunction(FunctionType.NUMERIC, "SIGN", "SIGN(%s)",
                    new FunctionDescriptor(Math.class.getMethod("signum", double.class), 1));

            // COLLECTION functions
            registerFunction(FunctionType.COLLECTION, "IN", "%s IN (%s)",
                    new FunctionDescriptor(Collection.class.getMethod("contains", Object.class), 1, 0));
            registerFunction(FunctionType.COLLECTION, "NOT IN", "%s NOT IN (%s)",
                    new FunctionDescriptor(Collection.class.getMethod("contains", Object.class), 1, 0));
            registerFunction(FunctionType.COLLECTION, "SIZE", "SIZE(%s)",
                    new FunctionDescriptor(Collection.class.getMethod("size"), 0));
            registerFunction(FunctionType.COLLECTION, "IS EMPTY", "%s IS EMPTY",
                    new FunctionDescriptor(Collection.class.getMethod("isEmpty"), 0));
            registerFunction(FunctionType.COLLECTION, "IS NOT EMPTY", "%s IS NOT EMPTY",
                    new FunctionDescriptor(Collection.class.getMethod("isEmpty"), 0));
            registerFunction(FunctionType.COLLECTION, "INDEX", "INDEX(%s)",
                    new FunctionDescriptor(List.class.getMethod("indexOf", Object.class), 0, 1));
            registerFunction(FunctionType.COLLECTION, "KEY", "KEY(%s)",
                    new FunctionDescriptor(Map.class.getMethod("keySet"), 0));

            // DATETIME functions
            registerFunction(FunctionType.DATETIME, "CURRENT_DATE", "CURRENT_DATE",
                    new FunctionDescriptor(LocalDate.class.getMethod("now")));
            registerFunction(FunctionType.DATETIME, "CURRENT_TIME", "CURRENT_TIME",
                    new FunctionDescriptor(LocalTime.class.getMethod("now")));
            registerFunction(FunctionType.DATETIME, "CURRENT_TIMESTAMP", "CURRENT_TIMESTAMP",
                    new FunctionDescriptor(LocalDateTime.class.getMethod("now")));
            registerFunction(FunctionType.DATETIME, "YEAR", "YEAR(%s)",
                    new FunctionDescriptor(LocalDate.class.getMethod("getYear"), 0));
            registerFunction(FunctionType.DATETIME, "MONTH", "MONTH(%s)",
                    new FunctionDescriptor(LocalDate.class.getMethod("getMonthValue"), 0));
            registerFunction(FunctionType.DATETIME, "DAY", "DAY(%s)",
                    new FunctionDescriptor(LocalDate.class.getMethod("getDayOfMonth"), 0));
            registerFunction(FunctionType.DATETIME, "HOUR", "HOUR(%s)",
                    new FunctionDescriptor(LocalTime.class.getMethod("getHour"), 0));
            registerFunction(FunctionType.DATETIME, "MINUTE", "MINUTE(%s)",
                    new FunctionDescriptor(LocalTime.class.getMethod("getMinute"), 0));
            registerFunction(FunctionType.DATETIME, "SECOND", "SECOND(%s)",
                    new FunctionDescriptor(LocalTime.class.getMethod("getSecond"), 0));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to register JPQL functions", e);
        }
    }

    private static void registerFunction(FunctionType type, String name, String expressionPattern, FunctionDescriptor... descriptors) {
        for (FunctionDescriptor descriptor : descriptors) {
            JpqlFunction function = new JpqlFunction(name, type, expressionPattern, descriptor);
            FUNCTION_MAP.computeIfAbsent(type, k -> new ArrayList<>()).add(function);
        }
    }

    private final String name;
    private final FunctionType type;
    private final String expressionPattern;
    private final FunctionDescriptor descriptor;

    public JpqlFunction(String name, FunctionType type, String expressionPattern, FunctionDescriptor descriptor) {
        this.name = name;
        this.type = type;
        this.expressionPattern = expressionPattern;
        this.descriptor = descriptor;
    }

    @Override
    public String toString() {
        return String.format("JPQL Function: %s (Type: %s, Expr: %s)", name, type, expressionPattern);
    }

    public static int locate(String substr, String str) {
        return str.indexOf(substr);
    }

    public static int length(String str) {
        return str.length();
    }

    public static String lower(String str) {
        return str.toLowerCase();
    }

    public static String upper(String str) {
        return str.toUpperCase();
    }

    public static boolean in(List<?> list, Object value) {
        return list.contains(value);
    }

    public static boolean notIn(List<?> list, Object value) {
        return !list.contains(value);
    }

    public static boolean between(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static boolean like(String str, String pattern) {
        return str.contains(pattern.replace("%", ""));
    }

    public static boolean isNull(Object obj) {
        return obj == null;
    }

    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    public static boolean exists(Object dummy) {
        throw new UnsupportedOperationException("EXISTS is not evaluatable.");
    }
}
