package org.lambdaql.analyzer;

import org.lambdaql.analyzer.grouping.ConditionGroup;
import org.lambdaql.query.QueryBuilder;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;


import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class LambdaMethodVisitor extends ClassVisitor {
    private final Method method;
    private final QueryBuilder queryBuilder;
    private final List<Class<?>> entityClasses;
    private final SerializedLambda serializedLambda;
    private ConditionGroup conditionExpr;

    private final String implMethod;

    public LambdaMethodVisitor(QueryBuilder queryBuilder, Method method, SerializedLambda serializedLambda, List<Class<?>> entityClasses) {
        super(ASM9);
        this.queryBuilder = queryBuilder;
        this.method = method;

        this.entityClasses = entityClasses;
        this.serializedLambda = serializedLambda;
        this.implMethod = serializedLambda.getImplMethodName();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals(implMethod)) {
            System.out.println(" > visitMethod: " + name + " " + desc+ " " + desc+ " signature " + signature+ " exceptions " + exceptions);
            System.out.println("serializedLambda getCapturingClass: "+serializedLambda.getCapturingClass());
            System.out.println("serializedLambda getFunctionalInterfaceClass: "+serializedLambda.getFunctionalInterfaceClass());
            System.out.println("serializedLambda getFunctionalInterfaceMethodName: "+serializedLambda.getFunctionalInterfaceMethodName());
            System.out.println("serializedLambda getFunctionalInterfaceMethodSignature: "+serializedLambda.getFunctionalInterfaceMethodSignature());
            System.out.println("serializedLambda getImplClass: "+serializedLambda.getImplClass());
            System.out.println("serializedLambda getImplMethodName: "+serializedLambda.getImplMethodName());
            System.out.println("serializedLambda getImplMethodSignature: "+serializedLambda.getImplMethodSignature());
            System.out.println("serializedLambda getInstantiatedMethodType: "+serializedLambda.getInstantiatedMethodType());

            Field[] declaredFields = this.method.getDeclaringClass().getDeclaredFields();
            LambdaVariableAnalyzer lambdaVariable = new LambdaVariableAnalyzer(this.method, serializedLambda, entityClasses, access);

            return new LambdaPredicateVisitor(queryBuilder, serializedLambda, lambdaVariable, access) {
                @Override
                public void visitEnd() {
                    super.visitEnd();
                    conditionExpr = getConditionExpr();
                }
            };
        }
        return null;
    }

    public ConditionGroup getConditionExpr() {
        return conditionExpr;
    }
}
