package org.lambdaql.function;

import lombok.Getter;

import java.lang.reflect.Method;

@Getter
public class FunctionDescriptor {
    private final Method method;
    private final int[] argMappings; // -1 = return, 0 = this, 1 = first arg, etc.

    public FunctionDescriptor(Method method, int... argMappings) {
        this.method = method;
        this.argMappings = argMappings;
    }
}
