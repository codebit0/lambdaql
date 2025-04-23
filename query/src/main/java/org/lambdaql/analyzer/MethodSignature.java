package org.lambdaql.analyzer;

import org.objectweb.asm.Type;

import java.lang.reflect.Method;

public record MethodSignature(Class<?>[] parameterTypes, Class<?> returnType) {

    public static Method parse(String owner, String name, String descriptor, boolean isInterface)  {
        Type methodType = Type.getMethodType(descriptor);

        String typeDescriptor = owner.replaceAll("/", ".");
        Type[] argumentTypes = methodType.getArgumentTypes();
        Class<?>[] parameterClasses = new Class<?>[argumentTypes.length];

        try {
            Class<?> klass = Class.forName(typeDescriptor);
            for (int i = 0; i < argumentTypes.length; i++) {
                parameterClasses[i] = typeToClass(argumentTypes[i]);
            }
            Class<?> returnType = typeToClass(methodType.getReturnType());
            Method method = klass.getMethod(name, parameterClasses);
            return method;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
