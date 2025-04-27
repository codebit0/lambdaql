package org.lambdaql.query;

import java.io.Serializable;

public class SelectJoin3Query<T, U1, U2> {

    public interface JoinOn<T,U1, U2> extends Serializable {
        boolean on(T baseEntity, U1 joinEntity1, U2 joinEntity2);
    }

    public SelectJoin3Query<T, U1, U2> leftJoin(Class<U1> entityClass, JoinOn<T, U1, U2> onCondition) {
        return new SelectJoin3Query<>();
    }
}
