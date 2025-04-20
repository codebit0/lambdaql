package org.lambdaql.analyzer;

import java.lang.reflect.Method;

public class ExecuteExpression {
    private final ObjectCapturedValue capturedValue;
    private final Method method;

    public ExecuteExpression(ObjectCapturedValue capturedValue, Method method) {
        this.capturedValue = capturedValue;
        this.method = method;
    }
}
