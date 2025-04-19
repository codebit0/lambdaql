package org.lambdaql.query;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import org.hibernate.metamodel.model.domain.internal.SingularAttributeImpl;
import org.lambdaql.query.lambda.LambdaVariableAnalyzer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;


import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class LambdaMethodVisitor extends ClassVisitor {
    private final Method method;
    private final Metamodel metamodel;
    private final List<Class<?>> entityClasses;
    private final SerializedLambda serializedLambda;
    private ConditionExpr conditionExpr;

    private String implMethod;

    public LambdaMethodVisitor(Method method, SerializedLambda serializedLambda, List<Class<?>> entityClasses, Metamodel metamodel) {
        super(ASM9);
        this.method = method;
        this.metamodel = metamodel;
        this.entityClasses = entityClasses;
        this.serializedLambda = serializedLambda;
        this.implMethod = serializedLambda.getImplMethodName();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals(implMethod)) {
            System.out.println(" > visitMethod: " + name + " " + desc+ " " + desc+ " signature " + signature+ " exceptions " + exceptions);

            ManagedType<?> managedType = metamodel.managedType(entityClasses.getFirst());
            managedType.getAttributes().stream().forEach(attr-> {

                if (attr instanceof SingularAttributeImpl) {
                    SingularAttributeImpl<?, ?> hAttr = (SingularAttributeImpl<?, ?>) attr;
                    System.out.println(hAttr);
                }
            });
            EntityType<?> entity = metamodel.entity(entityClasses.getFirst());
            entity.getAttributes().forEach( a-> {
                        System.out.println(a);
                    }
            );

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

            return new LambdaPredicateVisitor(serializedLambda, lambdaVariable, metamodel, access) {
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
