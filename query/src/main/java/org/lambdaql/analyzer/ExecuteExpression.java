package org.lambdaql.analyzer;

import lombok.Getter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ExecuteExpression {
    private final ObjectCapturedValue capturedValue;
    private final Method method;
    private List<Object> arguments = new ArrayList<>();

    public ExecuteExpression(ObjectCapturedValue capturedValue, Method method) {
        this.capturedValue = capturedValue;
        this.method = method;
    }

    public List<Object> addArguments(Object arg) {
        arguments.add(arg);
        return arguments;
    }
}
