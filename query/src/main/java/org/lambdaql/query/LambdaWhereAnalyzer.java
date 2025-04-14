package org.lambdaql.query;

import org.lambdaql.query.SelectQuery.Where;
import jakarta.persistence.EntityManager;
import org.objectweb.asm.*;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

public class LambdaWhereAnalyzer {

    private final EntityManager entityManager;
    private final Class<?> entityClass;

    public LambdaWhereAnalyzer(EntityManager entityManager, Class<?> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
    }

    public <T> ConditionExpr analyze(Where<T> whereClause) {
        try {
            Method method = whereClause.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            Object serialized = method.invoke(whereClause);

            if (serialized instanceof SerializedLambda sl) {
                String implMethod = sl.getImplMethodName();
                ClassReader reader = new ClassReader(sl.getImplClass().replace('/', '.'));
                LambdaMethodVisitor visitor = new LambdaMethodVisitor(entityManager.getMetamodel(), entityClass);
                visitor.implMethod = implMethod;
                reader.accept(visitor, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                return visitor.getConditionExpr();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new UnsupportedOperationException("Lambda parsing failed");
    }

    /*public static <T> String toWhereClause(SerializablePredicate<T> predicate) {
        try {
            SerializedLambda lambda = extractLambda(predicate);
            String implClass = lambda.getImplClass().replace('/', '.');
            String methodName = lambda.getImplMethodName();

            InputStream in = Class.forName(implClass)
                    .getResourceAsStream("/" + lambda.getImplClass() + ".class");

            ClassReader reader = new ClassReader(in);
            List<Condition> conditions = new ArrayList<>();
            AtomicReference<String> currentField = new AtomicReference<>();
            AtomicReference<Object> currentValue = new AtomicReference<>();
            AtomicReference<String> currentOp = new AtomicReference<>();
            AtomicReference<String> nextLogical = new AtomicReference<>(null);

            reader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] ex) {
                    if (!name.equals(methodName)) return null;

                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitLdcInsn(Object cst) {
                            currentValue.set(cst);
                        }

                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
                            if (name.startsWith("get")) {
                                String field = name.substring(3, 4).toLowerCase() + name.substring(4);
                                currentField.set(field);
                            } else if (name.equals("equals")) {
                                currentOp.set("=");
                            }
                        }

                        @Override
                        public void visitJumpInsn(int opcode, Label label) {
                            switch (opcode) {
                                case Opcodes.IF_ICMPEQ: currentOp.set("="); break;
                                case Opcodes.IF_ICMPNE: currentOp.set("!="); break;
                                case Opcodes.IF_ICMPGT: currentOp.set(">"); break;
                                case Opcodes.IF_ICMPLT: currentOp.set("<"); break;
                                case Opcodes.IF_ICMPGE: currentOp.set(">="); break;
                                case Opcodes.IF_ICMPLE: currentOp.set("<="); break;
                            }
                        }

                        @Override
                        public void visitInsn(int opcode) {
                            if (opcode == Opcodes.IRETURN || opcode == Opcodes.ARETURN) {
                                if (currentField.get() != null && currentOp.get() != null && currentValue.get() != null) {
                                    conditions.add(new Condition(
                                            currentField.get(),
                                            currentOp.get(),
                                            currentValue.get(),
                                            nextLogical.get()
                                    ));
                                    // reset for next condition
                                    currentField.set(null);
                                    currentOp.set(null);
                                    currentValue.set(null);
                                    nextLogical.set("AND"); // default for next
                                }
                            }
                        }

                        @Override
                        public void visitJumpInsn(int opcode, Label label) {
                            if (opcode == Opcodes.IFNE || opcode == Opcodes.IFEQ) {
                                // Simple boolean check
                            }
                        }

                        @Override
                        public void visitLabel(Label label) {
                            // Multiple labels appear for logical splits (AND, OR)
                        }

                        @Override
                        public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
                            // Stack frame splits may help detect OR
                        }
                    };
                }
            }, 0);

            return toSql(conditions);

        } catch (Exception e) {
            throw new RuntimeException("WHERE 분석 실패", e);
        }
    }

    private static String toSql(List<Condition> conds) {
        return conds.stream().map(c ->
                (c.logical != null ? c.logical + " " : "") +
                        c.field + " " + c.operator + " " + formatValue(c.value)
        ).collect(Collectors.joining(" "));
    }

    private static String formatValue(Object value) {
        return value instanceof String ? "'" + value + "'" : String.valueOf(value);
    }

    private static SerializedLambda extractLambda(Serializable lambda) throws Exception {
        Method m = lambda.getClass().getDeclaredMethod("writeReplace");
        m.setAccessible(true);
        return (SerializedLambda) m.invoke(lambda);
    }*/
}
