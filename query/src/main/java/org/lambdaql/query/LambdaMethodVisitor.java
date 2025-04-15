package org.lambdaql.query;

import jakarta.persistence.metamodel.Metamodel;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;


import java.lang.invoke.SerializedLambda;

import static org.objectweb.asm.Opcodes.*;

public class LambdaMethodVisitor extends ClassVisitor {
    private final Metamodel metamodel;
    private final Class<?> entityClass;
    private final SerializedLambda serializedLambda;
    private ConditionExpr conditionExpr;

    public String implMethod;

    public LambdaMethodVisitor(Metamodel metamodel, Class<?> entityClass, SerializedLambda serializedLambda) {
        super(ASM9);
        this.metamodel = metamodel;
        this.entityClass = entityClass;
        this.serializedLambda = serializedLambda;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals(implMethod)) {
            return new LambdaPredicateVisitor(metamodel, entityClass, serializedLambda) {
                @Override
                public void visitEnd() {
                    conditionExpr = getConditionExpr();
                    super.visitEnd();
                }
            };
        }
        return null;
    }

    public ConditionExpr getConditionExpr() {
        return conditionExpr;
    }
}
