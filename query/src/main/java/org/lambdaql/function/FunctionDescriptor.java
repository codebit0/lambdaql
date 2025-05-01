package org.lambdaql.function;

import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Getter
public class FunctionDescriptor {
    private final Method method;
    private final int[] argMappings; // -1 = return, 0 = this, 1 = first arg, etc.
    private final Field field;

    public FunctionDescriptor(Method method, int... argMappings) {
        this.method = method;
        this.field = null;
        this.argMappings = argMappings;
    }

    public FunctionDescriptor(Field field) {
        this.method = null;
        this.field = field;
        this.argMappings = new int[0];
    }
}
