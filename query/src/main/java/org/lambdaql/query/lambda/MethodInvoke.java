package org.lambdaql.query.lambda;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodInvoke {
    private final Class<?> clazz;          // 분석된 클래스 타입
    private final Method method;            // 분석된 Method 객체
    private List<Object> arguments = new ArrayList<>();   // 인자 (Constant, 또다른 MethodInvoke 가능)

    public MethodInvoke(Class<?> clazz, Method method, List<Object> arguments) {
        this.clazz = clazz;
        this.method = method;
        this.arguments = arguments;
    }

    public MethodInvoke(ICapturedValue capturedValue, Method method, List<Object> arguments) {
        this.clazz = capturedValue.type().getClass();
        this.method = method;
        this.arguments = arguments;
    }

    public MethodInvoke(ICapturedValue capturedValue, Method method) {
        this.clazz = capturedValue.type().getClass();
        this.method = method;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Method getMethod() {
        return method;
    }

    public List<Object> getArguments() {
        return arguments;
    }

    public List<Object> addArguments(Object arg) {
        arguments.add(arg);
        return arguments;
    }

    /**
     * MethodInvoke를 실행하여 실제 결과값을 반환합니다.
     */
    public Object execute() throws Exception {
        List<Object> evaluatedArgs = new ArrayList<>();

        // 인자 하나씩 재귀적으로 평가
        for (Object arg : arguments) {
            if (arg instanceof MethodInvoke nestedInvoke) {
                evaluatedArgs.add(nestedInvoke.execute());
            } else {
                evaluatedArgs.add(arg);
            }
        }

        method.setAccessible(true);

        // static 메서드 호출: instance = null
        // instance 메서드 호출: 첫 번째 evaluatedArgs를 instance로 보고 제거
        if ((method.getModifiers() & java.lang.reflect.Modifier.STATIC) != 0) {
            // static
            return method.invoke(null, evaluatedArgs.toArray());
        } else {
            // instance
            if (evaluatedArgs.isEmpty()) {
                throw new IllegalArgumentException("Instance method needs target object as first argument.");
            }
            Object target = evaluatedArgs.get(0);
            List<Object> realArgs = evaluatedArgs.subList(1, evaluatedArgs.size());
            return method.invoke(target, realArgs.toArray());
        }
    }
}
