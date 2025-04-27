package org.lambdaql.analyzer;

import org.lambdaql.query.QueryBuilder;
import org.lambdaql.query.SelectQuery.Where;
import org.objectweb.asm.*;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.List;

public class LambdaWhereAnalyzer {

    private final QueryBuilder queryBuilder;
    private final List<Class<?>> entityClass;

    public LambdaWhereAnalyzer(QueryBuilder queryBuilder, List<Class<?>> entityClass) {
        this.queryBuilder = queryBuilder;
        this.entityClass = entityClass;
    }

    public <T> ConditionExpression analyze(Where<T> whereClause) {
        try {
            Method method = whereClause.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            Object serialized = method.invoke(whereClause);

            if (serialized instanceof SerializedLambda sl) {
                ClassReader reader = new ClassReader(sl.getImplClass().replace('/', '.'));
                LambdaMethodVisitor visitor = new LambdaMethodVisitor(queryBuilder, method, sl, entityClass);
                reader.accept(visitor, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                return visitor.getConditionExpr();
            }
        } catch (Exception e) {
            /*try {
                String name = whereClause.getClass().getCanonicalName();
                if(name == null)
                    name = whereClause.getClass().getName();
                ClassReader reader = new ClassReader(name);
                reader.accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                        if (name.equals("clause")) {
                            System.out.println("Analyzing method: " + name + " " + descriptor);
                            return new MethodVisitor(Opcodes.ASM9) {
                                @Override
                                public void visitInsn(int opcode) {
                                    System.out.println("Instruction: ");
                                }

                                @Override
                                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                                    System.out.printf("Method call: %s -> %s.%s%s\n", "opcode", owner, name, descriptor);
                                }
                            };
                        }
                        return null;
                    }
                }, 0);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }*/
            e.printStackTrace();
        }
        throw new UnsupportedOperationException("Lambda parsing failed");
    }


}
