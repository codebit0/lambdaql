package org.lambdaql.query;

import jakarta.persistence.metamodel.Metamodel;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;


import java.lang.invoke.SerializedLambda;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class LambdaMethodVisitor extends ClassVisitor {
    private final Metamodel metamodel;
    private final List<Class<?>> entityClass;
    private final SerializedLambda serializedLambda;
    private ConditionExpr conditionExpr;

    public String implMethod;

    public LambdaMethodVisitor(Metamodel metamodel, List<Class<?>> entityClass, SerializedLambda serializedLambda) {
        super(ASM9);
        this.metamodel = metamodel;
        this.entityClass = entityClass;
        this.serializedLambda = serializedLambda;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals(implMethod)) {
            System.out.println(" > visitMethod: " + name + " " + desc+ " " + signature);
            return new LambdaPredicateVisitor(metamodel, entityClass, serializedLambda, access) {
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
