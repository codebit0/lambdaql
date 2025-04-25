package org.lambdaql.analyzer;

import lombok.Getter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ExecuteExpression extends MethodInvoke {
    private final ObjectCapturedVariable capturedValue;

    public ExecuteExpression(ObjectCapturedVariable capturedValue, Method method, List<Object> arguments) {
        super(capturedValue.value(), method, arguments);
        this.capturedValue = capturedValue;
    }

    public ExecuteExpression(ObjectCapturedVariable capturedValue, Method method) {
        this(capturedValue, method, new ArrayList<>());
    }

}
