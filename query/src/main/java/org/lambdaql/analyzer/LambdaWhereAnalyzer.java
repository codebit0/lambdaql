package org.lambdaql.analyzer;

import org.lambdaql.query.SelectQuery.Where;
import jakarta.persistence.EntityManager;
import org.objectweb.asm.*;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.List;

public class LambdaWhereAnalyzer {

    private final EntityManager entityManager;
    private final List<Class<?>> entityClass;

    public LambdaWhereAnalyzer(EntityManager entityManager, List<Class<?>> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
    }

    public <T> ConditionExpr analyze(Where<T> whereClause) {
        try {
            Method method = whereClause.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            Object serialized = method.invoke(whereClause);

            if (serialized instanceof SerializedLambda sl) {
                ClassReader reader = new ClassReader(sl.getImplClass().replace('/', '.'));
                LambdaMethodVisitor visitor = new LambdaMethodVisitor(method, sl, entityClass, entityManager.getMetamodel());
                reader.accept(visitor, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                return visitor.getConditionExpr();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new UnsupportedOperationException("Lambda parsing failed");
    }


}
