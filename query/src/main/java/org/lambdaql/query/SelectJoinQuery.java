package org.lambdaql.query;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class SelectJoinQuery<T, J1> {

    @FunctionalInterface
    public interface Where<T, J> extends Serializable {
        boolean where(T baseEntity, J joinEntity);
    }

    @FunctionalInterface
    public interface JoinOn<T, J> extends Serializable {
        boolean on(T t, J join1);
    }


    public SelectJoinQuery(SelectQuery<T> baseQuery, Class<J1> joinEntity, JoinOn<T, J1> onCondition) {
        // Constructor logic here

    }

    public <J2> SelectJoin3Query<T, J1, J2> leftJoin(Class<J2> entityClass, SelectJoin3Query.JoinOn<T, J1, J2> onCondition) {
        return new SelectJoin3Query<>();
    }

    public SelectJoinQuery<T, J1> where(Where<T, J1> condition) {
        return this;
    }

    @FunctionalInterface
    public interface Select<T, J, R> extends Serializable {
        R clause(T entity, J join);
    }

//    public <R> SelectJoinQuery<T, J1> select(Select<T, J1, R> clause) {
//        return this;
//    }

    public List<T> toList() {
        return Collections.emptyList();
    }
}
