package org.lambdaql.analyzer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodInvoke {
    private final Object target;                // 첫 번째: 호출 대상 (instance or null)
    private final Method method;                // 두 번째: 실행할 메서드
    private final List<Object> arguments;       // 세 번째: 메서드 인자들
    private final boolean isStatic;             // static 메서드 여부 저장

    public MethodInvoke(Object target, Method method, List<Object> arguments) {
        this.target = target;
        this.method = method;
        this.arguments = arguments != null ? arguments : new ArrayList<>();

        this.isStatic = (method.getModifiers() & java.lang.reflect.Modifier.STATIC) != 0;
    }

    public MethodInvoke(Object target, Method method) {
        this(target, method, new ArrayList<>());
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public List<Object> getArguments() {
        return arguments;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public MethodInvoke addArgument(Object arg) {
        this.arguments.add(arg);
        return this;
    }

    /**
     * MethodInvoke를 실행하여 실제 결과값을 반환합니다.
     */
    public Object execute() throws Exception {
        List<Object> evaluatedArgs = new ArrayList<>();

        for (Object arg : arguments) {
            if (arg instanceof MethodInvoke nestedInvoke) {
                evaluatedArgs.add(nestedInvoke.execute());
            } else {
                evaluatedArgs.add(arg);
            }
        }

        method.setAccessible(true);

        return method.invoke(target, evaluatedArgs.toArray());
    }
}