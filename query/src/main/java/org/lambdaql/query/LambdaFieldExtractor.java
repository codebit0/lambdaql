package org.lambdaql.query;

import aj.org.objectweb.asm.ClassReader;
import aj.org.objectweb.asm.ClassVisitor;
import aj.org.objectweb.asm.MethodVisitor;
import aj.org.objectweb.asm.Opcodes;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class LambdaFieldExtractor {

    public static <T> Map<String, Object> extractFields(Class<T> clazz, SerializableConsumer<T> consumer) {
        Map<String, Object> fields = new LinkedHashMap<>();

        try {
            SerializedLambda lambda = extractLambda(consumer);
            String implClass = lambda.getImplClass().replace('/', '.');
            String implMethod = lambda.getImplMethodName();

            InputStream in = Class.forName(implClass).getResourceAsStream("/" + lambda.getImplClass() + ".class");
            ClassReader reader = new ClassReader(in);

            reader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] ex) {
                    if (name.equals(implMethod)) {
                        return new MethodVisitor(Opcodes.ASM9) {
                            String currentField;
                            Object currentValue;

                            @Override
                            public void visitLdcInsn(Object value) {
                                currentValue = value;
                            }

                            @Override
                            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
                                if (name.startsWith("set")) {
                                    currentField = name.substring(3, 4).toLowerCase() + name.substring(4);
                                    fields.put(currentField, currentValue);
                                }
                            }
                        };
                    }
                    return null;
                }
            }, 0);

        } catch (Exception e) {
            throw new RuntimeException("람다 분석 실패", e);
        }

        return fields;
    }

    private static SerializedLambda extractLambda(Serializable lambda) throws Exception {
        Method writeReplace = lambda.getClass().getDeclaredMethod("writeReplace");
        writeReplace.setAccessible(true);
        return (SerializedLambda) writeReplace.invoke(lambda);
    }
}
