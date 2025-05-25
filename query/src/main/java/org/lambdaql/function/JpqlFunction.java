package org.lambdaql.function;

import lombok.Getter;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.time.*;
import java.time.chrono.ChronoZonedDateTime;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

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
            // COLLECTION functions
            registerFunction(FunctionType.COLLECTION, "IN", "%s IN (%s)",
                    new FunctionDescriptor(Collection.class.getMethod("contains", Object.class), 1, 0));
            registerFunction(FunctionType.COLLECTION, "INDEX", "INDEX(%s)",
                    new FunctionDescriptor(List.class.getMethod("indexOf", Object.class), 0, 1));
            registerFunction(FunctionType.COLLECTION, "IS EMPTY", "%s IS EMPTY",
                    new FunctionDescriptor(Collection.class.getMethod("isEmpty"), 0));
            registerFunction(FunctionType.COLLECTION, "IS NOT EMPTY", "%s IS NOT EMPTY",
                    new FunctionDescriptor(Collection.class.getMethod("isEmpty"), 0));
            registerFunction(FunctionType.COLLECTION, "KEY", "KEY(%s)",
                    new FunctionDescriptor(Map.class.getMethod("keySet"), 0));
            registerFunction(FunctionType.COLLECTION, "NOT IN", "%s NOT IN (%s)",
                    new FunctionDescriptor(Collection.class.getMethod("contains", Object.class), 1, 0));
            registerFunction(FunctionType.COLLECTION, "SIZE", "SIZE(%s)",
                    new FunctionDescriptor(Collection.class.getMethod("size"), 0));
//            registerFunction(FunctionType.COLLECTION, "SIZE", "SIZE(%s)",
//                    new FunctionDescriptor(Object[].class.getField("length")));
            registerFunction(FunctionType.COLLECTION, "SIZE", "SIZE(%s)",
                    new FunctionDescriptor(Array.class.getMethod("getLength", Object.class), 0));

            // DATETIME functions
            // LocalDate, LocalTime, LocalDateTime, OffsetDateTime, ZonedDateTime, Instant, ChronoZonedDateTime
            // CURRENT_DATE variants
            registerFunction(FunctionType.DATETIME, "CURRENT_DATE", "CURRENT_DATE",
                    new FunctionDescriptor(LocalDate.class.getMethod("now")));
            registerFunction(FunctionType.DATETIME, "CURRENT_DATE", "CURRENT_DATE",
                    new FunctionDescriptor(OffsetDateTime.class.getMethod("now")));
            registerFunction(FunctionType.DATETIME, "CURRENT_DATE", "CURRENT_DATE",
                    new FunctionDescriptor(ZonedDateTime.class.getMethod("now")));

            // CURRENT_TIME variants
            registerFunction(FunctionType.DATETIME, "CURRENT_TIME", "CURRENT_TIME",
                    new FunctionDescriptor(LocalTime.class.getMethod("now")));
            registerFunction(FunctionType.DATETIME, "CURRENT_TIME", "CURRENT_TIME",
                    new FunctionDescriptor(OffsetDateTime.class.getMethod("now")));
            registerFunction(FunctionType.DATETIME, "CURRENT_TIME", "CURRENT_TIME",
                    new FunctionDescriptor(ZonedDateTime.class.getMethod("now")));

            // CURRENT_TIMESTAMP variants
            registerFunction(FunctionType.DATETIME, "CURRENT_TIMESTAMP", "CURRENT_TIMESTAMP",
                    new FunctionDescriptor(LocalDateTime.class.getMethod("now")));
            registerFunction(FunctionType.DATETIME, "CURRENT_TIMESTAMP", "CURRENT_TIMESTAMP",
                    new FunctionDescriptor(OffsetDateTime.class.getMethod("now")));
            registerFunction(FunctionType.DATETIME, "CURRENT_TIMESTAMP", "CURRENT_TIMESTAMP",
                    new FunctionDescriptor(ZonedDateTime.class.getMethod("now")));
            registerFunction(FunctionType.DATETIME, "CURRENT_TIMESTAMP", "CURRENT_TIMESTAMP",
                    new FunctionDescriptor(Instant.class.getMethod("now")));

            // DATETIME function registrations (explicit per type)
            registerFunction(FunctionType.DATETIME, "YEAR", "YEAR(%s)",
                    new FunctionDescriptor(LocalDate.class.getMethod("getYear"), 0));
            registerFunction(FunctionType.DATETIME, "YEAR", "YEAR(%s)",
                    new FunctionDescriptor(LocalDateTime.class.getMethod("getYear"), 0));
            registerFunction(FunctionType.DATETIME, "YEAR", "YEAR(%s)",
                    new FunctionDescriptor(OffsetDateTime.class.getMethod("getYear"), 0));
            registerFunction(FunctionType.DATETIME, "YEAR", "YEAR(%s)",
                    new FunctionDescriptor(ZonedDateTime.class.getMethod("getYear"), 0));
            registerFunction(FunctionType.DATETIME, "YEAR", "YEAR(%s)",
                    new FunctionDescriptor(ChronoZonedDateTime.class.getMethod("getYear"), 0));

            registerFunction(FunctionType.DATETIME, "MONTH", "MONTH(%s)",
                    new FunctionDescriptor(LocalDate.class.getMethod("getMonthValue"), 0));
            registerFunction(FunctionType.DATETIME, "MONTH", "MONTH(%s)",
                    new FunctionDescriptor(LocalDateTime.class.getMethod("getMonthValue"), 0));
            registerFunction(FunctionType.DATETIME, "MONTH", "MONTH(%s)",
                    new FunctionDescriptor(OffsetDateTime.class.getMethod("getMonthValue"), 0));
            registerFunction(FunctionType.DATETIME, "MONTH", "MONTH(%s)",
                    new FunctionDescriptor(ZonedDateTime.class.getMethod("getMonthValue"), 0));

            registerFunction(FunctionType.DATETIME, "DAY", "DAY(%s)",
                    new FunctionDescriptor(LocalDate.class.getMethod("getDayOfMonth"), 0));
            registerFunction(FunctionType.DATETIME, "DAY", "DAY(%s)",
                    new FunctionDescriptor(LocalDateTime.class.getMethod("getDayOfMonth"), 0));
            registerFunction(FunctionType.DATETIME, "DAY", "DAY(%s)",
                    new FunctionDescriptor(OffsetDateTime.class.getMethod("getDayOfMonth"), 0));
            registerFunction(FunctionType.DATETIME, "DAY", "DAY(%s)",
                    new FunctionDescriptor(ZonedDateTime.class.getMethod("getDayOfMonth"), 0));

            registerFunction(FunctionType.DATETIME, "HOUR", "HOUR(%s)",
                    new FunctionDescriptor(LocalTime.class.getMethod("getHour"), 0));
            registerFunction(FunctionType.DATETIME, "HOUR", "HOUR(%s)",
                    new FunctionDescriptor(LocalDateTime.class.getMethod("getHour"), 0));
            registerFunction(FunctionType.DATETIME, "HOUR", "HOUR(%s)",
                    new FunctionDescriptor(OffsetDateTime.class.getMethod("getHour"), 0));
            registerFunction(FunctionType.DATETIME, "HOUR", "HOUR(%s)",
                    new FunctionDescriptor(ZonedDateTime.class.getMethod("getHour"), 0));

            registerFunction(FunctionType.DATETIME, "MINUTE", "MINUTE(%s)",
                    new FunctionDescriptor(LocalTime.class.getMethod("getMinute"), 0));
            registerFunction(FunctionType.DATETIME, "MINUTE", "MINUTE(%s)",
                    new FunctionDescriptor(LocalDateTime.class.getMethod("getMinute"), 0));
            registerFunction(FunctionType.DATETIME, "MINUTE", "MINUTE(%s)",
                    new FunctionDescriptor(OffsetDateTime.class.getMethod("getMinute"), 0));
            registerFunction(FunctionType.DATETIME, "MINUTE", "MINUTE(%s)",
                    new FunctionDescriptor(ZonedDateTime.class.getMethod("getMinute"), 0));

            registerFunction(FunctionType.DATETIME, "SECOND", "SECOND(%s)",
                    new FunctionDescriptor(LocalTime.class.getMethod("getSecond"), 0));
            registerFunction(FunctionType.DATETIME, "SECOND", "SECOND(%s)",
                    new FunctionDescriptor(LocalDateTime.class.getMethod("getSecond"), 0));
            registerFunction(FunctionType.DATETIME, "SECOND", "SECOND(%s)",
                    new FunctionDescriptor(OffsetDateTime.class.getMethod("getSecond"), 0));
            registerFunction(FunctionType.DATETIME, "SECOND", "SECOND(%s)",
                    new FunctionDescriptor(ZonedDateTime.class.getMethod("getSecond"), 0));

            registerFunction(FunctionType.DATETIME, "EXTRACT", "EXTRACT(%s FROM %s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("extract", String.class, Temporal.class), 0, 1));

            // NUMERIC functions
            registerFunction(FunctionType.NUMERIC, "ABS", "ABS(%s)",
                    new FunctionDescriptor(Math.class.getMethod("abs", int.class), 1));
            registerFunction(FunctionType.NUMERIC, "ABS", "ABS(%s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("abs", Number.class), 0));
            registerFunction(FunctionType.NUMERIC, "CEIL", "CEIL(%s)",
                    new FunctionDescriptor(Math.class.getMethod("ceil", double.class), 1));
            registerFunction(FunctionType.NUMERIC, "EXP", "EXP(%s)",
                    new FunctionDescriptor(Math.class.getMethod("exp", double.class), 1));
            registerFunction(FunctionType.NUMERIC, "FLOOR", "FLOOR(%s)",
                    new FunctionDescriptor(Math.class.getMethod("floor", double.class), 1));
            registerFunction(FunctionType.NUMERIC, "LOG", "LOG(%s)",
                    new FunctionDescriptor(Math.class.getMethod("log", double.class), 1));
            registerFunction(FunctionType.NUMERIC, "MOD", "MOD(%s, %s)",
                    new FunctionDescriptor(Math.class.getMethod("floorMod", int.class, int.class), 1, 2));
            registerFunction(FunctionType.NUMERIC, "MOD", "MOD(%s, %s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("mod", int.class, int.class), 0, 1));
            registerFunction(FunctionType.NUMERIC, "POWER", "POWER(%s, %s)",
                    new FunctionDescriptor(Math.class.getMethod("pow", double.class, double.class), 1, 2));
            registerFunction(FunctionType.NUMERIC, "ROUND", "ROUND(%s)",
                    new FunctionDescriptor(Math.class.getMethod("round", double.class), 1));
            registerFunction(FunctionType.NUMERIC, "SIGN", "SIGN(%s)",
                    new FunctionDescriptor(Math.class.getMethod("signum", double.class), 1));
            registerFunction(FunctionType.NUMERIC, "SQRT", "SQRT(%s)",
                    new FunctionDescriptor(Math.class.getMethod("sqrt", double.class), 1));
            registerFunction(FunctionType.NUMERIC, "SQRT", "SQRT(%s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("sqrt", Number.class), 0));

            // OPERATOR functions
            registerFunction(FunctionType.OPERATOR, "ALL", "%s = ALL(%s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("all", Object.class), 0));
            registerFunction(FunctionType.OPERATOR, "ANY", "%s = ANY(%s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("any", Object.class), 0));
            registerFunction(FunctionType.OPERATOR, "CAST", "CAST(%s AS %s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("cast", Object.class, Class.class), 0, 1));
            registerFunction(FunctionType.OPERATOR, "COALESCE", "COALESCE(%s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("coalesce", Object[].class), 0));
            registerFunction(FunctionType.OPERATOR, "EXISTS", "EXISTS(%s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("exists", Object.class), 0));
            registerFunction(FunctionType.OPERATOR, "IF", "FUNCTION('IF', %s, %s, %s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("ifFunc", Object.class, Object.class, Object.class), 0, 1, 2));
            registerFunction(FunctionType.OPERATOR, "IFNULL", "IFNULL(%s, %s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("ifNull", Object.class, Object.class), 0, 1));
            registerFunction(FunctionType.OPERATOR, "IS NOT NULL", "%s IS NOT NULL",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("isNotNull", Object.class), 0));
            registerFunction(FunctionType.OPERATOR, "IS NULL", "%s IS NULL",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("isNull", Object.class), 0));
            registerFunction(FunctionType.OPERATOR, "NULLIF", "NULLIF(%s, %s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("nullIf", Object.class, Object.class), 0, 1));
            registerFunction(FunctionType.OPERATOR, "BETWEEN", "%s BETWEEN %s AND %s",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("between", Comparable.class, Comparable.class, Comparable.class), 0, 1, 2));

            // STRING functions
            registerFunction(FunctionType.STRING, "CONCAT", "CONCAT(%s, %s)",
                    new FunctionDescriptor(String.class.getMethod("concat", String.class), 0, 1));
            registerFunction(FunctionType.STRING, "LENGTH", "LENGTH(%s)",
                    new FunctionDescriptor(String.class.getMethod("length"), 0));
            registerFunction(FunctionType.STRING, "LIKE", "%s LIKE %s",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("like", String.class, String.class), 0, 1));
            registerFunction(FunctionType.STRING, "LOCATE", "LOCATE(%s, %s)",
                    new FunctionDescriptor(String.class.getMethod("indexOf", String.class), 0, 1));
            registerFunction(FunctionType.STRING, "LOWER", "LOWER(%s)",
                    new FunctionDescriptor(String.class.getMethod("toLowerCase"), 0));
            registerFunction(FunctionType.STRING, "SUBSTRING", "SUBSTRING(%s, %s, %s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("substring", String.class, int.class, int.class), 0, 1, 2));
            registerFunction(FunctionType.STRING, "TRIM", "TRIM(%s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("trim", String.class), 0));
            registerFunction(FunctionType.STRING, "LTRIM", "TRIM(LEADING FROM %s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("ltrim", String.class), 0));
            registerFunction(FunctionType.STRING, "RTRIM", "TRIM(TRAILING FROM %s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("rtrim", String.class), 0));
            // 사용자 지정 문자 제거 버전
            registerFunction(FunctionType.STRING, "LTRIM", "TRIM(LEADING '%s' FROM %s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("ltrim", char.class, String.class), 0, 1));
            registerFunction(FunctionType.STRING, "RTRIM", "TRIM(TRAILING '%s' FROM %s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("rtrim", char.class, String.class), 0, 1));
            registerFunction(FunctionType.STRING, "TRIM", "TRIM(BOTH '%s' FROM %s)",
                    new FunctionDescriptor(JpqlFunction.class.getMethod("trim", char.class, String.class), 0, 1));

            registerFunction(FunctionType.STRING, "UPPER", "UPPER(%s)",
                    new FunctionDescriptor(String.class.getMethod("toUpperCase"), 0));

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
    /**
     * 지정된 메서드에 해당하는 JPQL 함수를 찾습니다.
     *
     * @param targetMethod 검색할 메서드
     * @return 해당하는 JPQL 함수, 없으면 null
     */
    //FIXME 메서드 public 제거
    public static JpqlFunction findJpqlFunction(Method targetMethod) {
        for (List<JpqlFunction> functions : FUNCTION_MAP.values()) {
            for (JpqlFunction function : functions) {
                FunctionDescriptor descriptor = function.getDescriptor();
                if (descriptor.getMethod() != null && descriptor.getMethod().equals(targetMethod)) {
                    return function;
                }
            }
        }
        return null;
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

    /**
     * JPQL TRIM 함수. 문자열의 양쪽 공백을 제거합니다.
     *
     * @param str 원본 문자열
     * @return 공백 제거된 문자열
     */
    public static String trim(String str) {
        return str != null ? str.trim() : null;
    }

    /**
     * 문자열의 왼쪽 공백을 제거합니다 (정규식: ^\s+).
     *
     * @param str 원본 문자열
     * @return 왼쪽 공백이 제거된 문자열
     */
    public static String ltrim(String str) {
        return str == null ? null : str.replaceAll("^\\s+", "");
    }

    /**
     * 문자열의 오른쪽 공백을 제거합니다 (정규식: \s+$).
     *
     * @param str 원본 문자열
     * @return 오른쪽 공백이 제거된 문자열
     */
    public static String rtrim(String str) {
        return str == null ? null : str.replaceAll("\\s+$", "");
    }

    /**
     * 지정한 문자로 시작하는 부분을 왼쪽에서 제거합니다.
     *
     * @param ch 제거할 문자
     * @param str 원본 문자열
     * @return 왼쪽에서 지정 문자가 제거된 문자열
     */
    public static String ltrim(char ch, String str) {
        if (str == null) return null;
        return str.replaceAll("^[" + Pattern.quote(String.valueOf(ch)) + "]+", "");
    }

    /**
     * 지정한 문자로 끝나는 부분을 오른쪽에서 제거합니다.
     *
     * @param ch 제거할 문자
     * @param str 원본 문자열
     * @return 오른쪽에서 지정 문자가 제거된 문자열
     */
    public static String rtrim(char ch, String str) {
        if (str == null) return null;
        return str.replaceAll("[" + Pattern.quote(String.valueOf(ch)) + "]+$", "");
    }

    /**
     * 지정한 문자로 시작하거나 끝나는 부분을 양쪽에서 제거합니다.
     *
     * @param ch 제거할 문자
     * @param str 원본 문자열
     * @return 양쪽에서 지정 문자가 제거된 문자열
     */
    public static String trim(char ch, String str) {
        if (str == null) return null;
        return str.replaceAll("^[" + Pattern.quote(String.valueOf(ch)) + "]+|[" + Pattern.quote(String.valueOf(ch)) + "]+$", "");
    }


    /**
     * JPQL LOCATE 함수에 해당.
     * 주어진 문자열(str)에서 부분 문자열(substr)이 처음 나타나는 위치를 반환합니다.
     *
     * @param substr 찾을 부분 문자열
     * @param str 대상 문자열
     * @return 부분 문자열의 시작 인덱스 (0부터 시작)
     */
    public static int locate(String substr, String str) {
        return str.indexOf(substr);
    }

    /**
     * JPQL LENGTH 함수에 해당.
     * 주어진 문자열의 길이를 반환합니다.
     *
     * @param str 문자열
     * @return 문자열의 길이
     */
    public static int length(String str) {
        return str.length();
    }

    /**
     * JPQL LOWER 함수에 해당.
     * 문자열을 소문자로 변환합니다.
     *
     * @param str 원본 문자열
     * @return 소문자로 변환된 문자열
     */
    public static String lower(String str) {
        return str.toLowerCase();
    }

    /**
     * JPQL UPPER 함수에 해당.
     * 문자열을 대문자로 변환합니다.
     *
     * @param str 원본 문자열
     * @return 대문자로 변환된 문자열
     */
    public static String upper(String str) {
        return str.toUpperCase();
    }

    /**
     * JPQL SUBSTRING 함수. 문자열에서 특정 위치부터 지정된 길이만큼 잘라냅니다.
     *
     * @param str 원본 문자열
     * @param start 시작 위치 (1-based)
     * @param length 잘라낼 길이
     * @return 결과 문자열
     */
    public static String substring(String str, int start, int length) {
        if (str == null) return null;
        int begin = Math.max(0, start - 1);
        int end = Math.min(str.length(), begin + length);
        return str.substring(begin, end);
    }


    /**
     * JPQL IN 연산자에 해당.
     * 주어진 값이 리스트에 포함되어 있는지 확인합니다.
     *
     * @param value 검사할 값
     * @param list 포함 여부를 확인할 리스트
     * @return 포함되어 있으면 true, 아니면 false
     */
    public static boolean in(Object value, List<?> list) {
        return list.contains(value);
    }

    /**
     * JPQL NOT IN 연산자에 해당.
     * 주어진 값이 리스트에 포함되어 있지 않은지 확인합니다.
     *
     * @param value 검사할 값
     * @param list 포함 여부를 확인할 리스트
     * @return 포함되어 있지 않으면 true, 포함되어 있으면 false
     */
    public static boolean notIn(Object value, List<?> list) {
        return !list.contains(value);
    }

    /**
     * JPQL BETWEEN 연산자에 해당.
     * 값이 지정된 범위 내에 있는지 확인합니다.
     *
     * @param value 검사할 값
     * @param min 하한값 (포함)
     * @param max 상한값 (포함)
     * @return 범위 내에 있으면 true, 아니면 false
     */
    public static <T extends Comparable<T>> boolean between(T value, T min, T max) {
        return value != null && min != null && max != null
                && value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    /**
     * JPQL LIKE 연산자에 해당.
     * 문자열이 패턴과 일치하는지 확인합니다. (단순 구현: '%' 와일드카드 처리만 반영)
     *
     * @param str 검사할 문자열
     * @param pattern JPQL 패턴 (예: "%abc%")
     * @return 패턴이 포함되어 있으면 true
     */
    public static boolean like(String str, String pattern) {
        return str.contains(pattern.replace("%", ""));
    }

    /**
     * JPQL IS NULL 연산자에 해당.
     * 객체가 null인지 여부를 확인합니다.
     *
     * @param obj 검사할 객체
     * @return null이면 true
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * JPQL IS NOT NULL 연산자에 해당.
     * 객체가 null이 아닌지 여부를 확인합니다.
     *
     * @param obj 검사할 객체
     * @return null이 아니면 true
     */
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    /**
     * JPQL EXISTS 연산자와 유사하게 동작합니다.
     * 인자가 null이거나 false 또는 0 이하인 숫자이면 false를 반환합니다.
     *
     * @param input 검사할 값 (Boolean, Number 등)
     * @return 조건을 만족하면 true, 그렇지 않으면 false
     */
    public static boolean exists(Object input) {
        if (input == null) return false;
        if (input instanceof Boolean bool) return bool;
        if (input instanceof Number num) return num.doubleValue() > 0;
        return true;
    }

    /**
     * JPQL에서의 IF(expr, trueVal, falseVal) 형태와 유사.
     * expr이 true이면 trueVal 반환, 아니면 falseVal 반환.
     *
     * @param condition 조건식 (Boolean 또는 Number)
     * @param trueVal 조건이 true일 때 반환할 값
     * @param falseVal 조건이 false일 때 반환할 값
     * @return 조건에 따라 선택된 값
     */
    public static Object ifFunc(Object condition, Object trueVal, Object falseVal) {
        boolean result = false;
        if (condition instanceof Boolean b) result = b;
        else if (condition instanceof Number n) result = n.doubleValue() != 0;
        return result ? trueVal : falseVal;
    }

    /**
     * JPQL의 IFNULL(expr1, expr2) 함수처럼, expr1이 null이면 expr2를 반환.
     *
     * @param first 검사할 값
     * @param second 대체할 값
     * @return first가 null이 아니면 first, 그렇지 않으면 second
     */
    public static Object ifNull(Object first, Object second) {
        return first != null ? first : second;
    }

    /**
     * JPQL NULLIF 함수. a와 b가 같으면 null, 아니면 a를 반환합니다.
     *
     * @param a 비교 대상 1
     * @param b 비교 대상 2
     * @return a == b 이면 null, 아니면 a
     */
    public static Object nullIf(Object a, Object b) {
        return Objects.equals(a, b) ? null : a;
    }

    /**
     * JPQL COALESCE(expr1, expr2, ...) 함수처럼, 첫 번째 null이 아닌 값을 반환.
     *
     * @param values 검사할 값들
     * @return null이 아닌 첫 번째 값, 모두 null이면 null
     */
    public static Object coalesce(Object... values) {
        for (Object val : values) {
            if (val != null) return val;
        }
        return null;
    }

    /**
     * JPQL ABS 함수. 숫자의 절댓값을 반환합니다.
     *
     * @param number 입력 숫자
     * @return 절댓값
     */
    public static Number abs(Number number) {
        if (number instanceof Integer i) return Math.abs(i);
        if (number instanceof Long l) return Math.abs(l);
        if (number instanceof Double d) return Math.abs(d);
        if (number instanceof Float f) return Math.abs(f);
        return number;
    }

    /**
     * JPQL SQRT 함수. 제곱근을 반환합니다.
     *
     * @param number 입력 숫자
     * @return 제곱근
     */
    public static double sqrt(Number number) {
        return Math.sqrt(number.doubleValue());
    }

    /**
     * JPQL MOD 함수. a % b를 반환합니다.
     *
     * @param a 피제수
     * @param b 제수
     * @return 나머지
     */
    public static int mod(int a, int b) {
        return a % b;
    }

    /**
     * JPQL ALL 연산자 (서브쿼리 필요). 평가 불가.
     */
    public static boolean all(Object dummy) {
        throw new UnsupportedOperationException("ALL is not evaluatable in memory.");
    }

    /**
     * JPQL ANY 연산자 (서브쿼리 필요). 평가 불가.
     */
    public static boolean any(Object dummy) {
        throw new UnsupportedOperationException("ANY is not evaluatable in memory.");
    }

    /**
     * 입력 값을 지정된 타입으로 캐스팅합니다.
     *
     * @param value      변환할 원본 값
     * @param targetType 변환할 대상 클래스 타입
     * @param <T>        대상 타입 제네릭
     * @return 변환된 값
     * @throws IllegalArgumentException 변환 불가한 경우
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object value, Class<T> targetType) {
        if (value == null || targetType == null) return null;
        if (targetType.isInstance(value)) return (T) value;

        try {
            if (targetType == String.class) return (T) value.toString();
            if (targetType == Integer.class || targetType == int.class) return (T) Integer.valueOf(value.toString());
            if (targetType == Long.class || targetType == long.class) return (T) Long.valueOf(value.toString());
            if (targetType == Double.class || targetType == double.class) return (T) Double.valueOf(value.toString());
            if (targetType == Float.class || targetType == float.class) return (T) Float.valueOf(value.toString());
            if (targetType == Short.class || targetType == short.class) return (T) Short.valueOf(value.toString());
            if (targetType == Byte.class || targetType == byte.class) return (T) Byte.valueOf(value.toString());
            if (targetType == Boolean.class || targetType == boolean.class) return (T) Boolean.valueOf(value.toString());
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot cast " + value + " to " + targetType.getSimpleName(), e);
        }

        throw new UnsupportedOperationException("Unsupported cast to: " + targetType.getSimpleName());
    }


    /**
     * JPQL EXTRACT(field FROM date) 함수.
     *
     * @param field "YEAR", "MONTH", "DAY", "HOUR", "MINUTE", "SECOND"
     * @param date 대상 날짜/시간
     * @return 해당 필드 값
     */
    public static int extract(String field, Temporal date) {
        if (date == null) return -1;
        return switch (field.toUpperCase()) {
            case "YEAR" -> ((LocalDateTime) date).getYear();
            case "MONTH" -> ((LocalDateTime) date).getMonthValue();
            case "DAY" -> ((LocalDateTime) date).getDayOfMonth();
            case "HOUR" -> ((LocalDateTime) date).getHour();
            case "MINUTE" -> ((LocalDateTime) date).getMinute();
            case "SECOND" -> ((LocalDateTime) date).getSecond();
            default -> -1;
        };
    }

    /**
     * Java에서 CASE WHEN 구문처럼 사용할 수 있는 함수.
     *
     * @param inputSupplier 입력값 공급자 (지연 평가)
     * @param defaultValue 조건이 모두 false일 때 반환할 기본값
     * @param cases 조건-결과 쌍 리스트
     * @param <T> 입력 타입
     * @param <R> 결과 타입
     * @return 매칭된 결과값 또는 기본값
     */
    @SafeVarargs
    public static <T, R> R caseWhen(Supplier<T> inputSupplier, R defaultValue, WhenClause<T, R>... cases) {
        T input = inputSupplier.get();
        for (WhenClause<T, R> clause : cases) {
            R result = clause.applyIf(input);
            if (result != null) return result;
        }
        return defaultValue;
    }

    @SafeVarargs
    public static <R> R caseWhen(R defaultValue, WhenClause<Void, R>... clauses) {
        for (WhenClause<Void, R> clause : clauses) {
            R result = clause.applyIf(null);
            if (result != null) return result;
        }
        return defaultValue;
    }


    @FunctionalInterface
    public interface WhenClause<T, R> {
        /**
         * 조건이 만족되면 결과를 반환하고, 만족되지 않으면 null 또는 Optional 등을 반환
         */
        R applyIf(T input);

        static <T, R> WhenClause<T, R> of(Predicate<T> condition, Function<T, R> result) {
            return input -> condition.test(input) ? result.apply(input) : null;
        }

        /**
         * 입력값 없는 CASE: WHEN (boolean) THEN (R)
         */
        static <R> WhenClause<Void, R> of(boolean condition, R result) {
            return input -> condition ? result : null;
        }

        /**
         * 입력값 없는 CASE: WHEN (BooleanSupplier) THEN (R)
         */
        static <R> WhenClause<Void, R> of(BooleanSupplier condition, R result) {
            return input -> condition.getAsBoolean() ? result : null;
        }
    }

}
