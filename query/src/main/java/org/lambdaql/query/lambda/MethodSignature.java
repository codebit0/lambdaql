package org.lambdaql.query.lambda;

import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record MethodSignature(Class<?>[] parameterTypes, Class<?> returnType) {

    public static MethodSignature parse(String descriptor) throws ClassNotFoundException {
        Type methodType = Type.getMethodType(descriptor);

        Type[] argumentTypes = methodType.getArgumentTypes();
        Class<?>[] parameterClasses = new Class<?>[argumentTypes.length];

        for (int i = 0; i < argumentTypes.length; i++) {
            parameterClasses[i] = typeToClass(argumentTypes[i]);
        }
        Class<?> returnType = typeToClass(methodType.getReturnType());

        return new MethodSignature(parameterClasses, returnType);
    }


    static Class<?> typeToClass(Type type) throws ClassNotFoundException {
        return switch (type.getSort()) {
            case Type.VOID -> void.class;
            case Type.BOOLEAN -> boolean.class;
            case Type.CHAR -> char.class;
            case Type.BYTE -> byte.class;
            case Type.SHORT -> short.class;
            case Type.INT -> int.class;
            case Type.FLOAT -> float.class;
            case Type.LONG -> long.class;
            case Type.DOUBLE -> double.class;
            case Type.ARRAY -> Class.forName(type.getDescriptor().replace('/', '.'));
            case Type.OBJECT -> Class.forName(type.getClassName());
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }
}
