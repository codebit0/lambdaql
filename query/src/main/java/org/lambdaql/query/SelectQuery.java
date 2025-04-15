package org.lambdaql.query;

import java.io.Serializable;
import java.util.List;

public class SelectQuery<T> {

    private final Class<T> entityClass;
    private final EntityColumnResolver columnResolver;
    private ConditionExpr condition;

    @FunctionalInterface
    public interface Where<U> extends Serializable {
        boolean clause(U obj);
    }

    @FunctionalInterface
    public interface JoinOn<T,U> extends Serializable {
        boolean on(T t, U join);
    }

    @FunctionalInterface
    public interface Select<T,R> extends Serializable {
        R clause(T entity);
    }

    public SelectQuery(Class<T> entityClass, EntityColumnResolver columnResolver) {
        this.entityClass = entityClass;
        this.columnResolver = columnResolver;
    }

    public <R> SelectQuery<T> select(Select<T, R> clause) {
        return this;
    }

    public SelectQuery<T> where(Where<T> condition) {
        this.condition = new LambdaWhereAnalyzer(columnResolver.getEntityManagerFactory().createEntityManager(), List.of(entityClass)).analyze(condition);
        return this;
    }

    public <U> SelectQuery<T> join(Class<U> entityClass, String onCondition) {
        return this;
    }

    public <U> SelectJoinQuery<T, U> leftJoin(Class<U> entityClass, JoinOn<T,U> onCondition) {
        return new SelectJoinQuery<>();
    }

    public SelectQuery<T> rightJoin(String table, String onCondition) {
        return this;
    }

    public SelectQuery<T> fullJoin(Class<?> entityClass) {
        return this;
    }

    public SelectQuery<T> orderBy(String field) {
        return this;
    }

    public SelectQuery<T> orderByDesc(String field) {
        return this;
    }

//    public SelectQuery<T> limit(int limit) {
//        return this;
//    }
//
//    public SelectQuery<T> offset(int offset) {
//        return this;
//    }

    public SelectQuery<T> groupBy(String field) {
        return this;
    }

    public SelectQuery<T> having(ConditionExpr condition) {
        return this;
    }

    public String toSql() {
        if (condition == null) return "";
        return "SELECT * FROM " + entityClass.getSimpleName().toLowerCase() +
                " WHERE "+Renderer.toSql("");
    }


    public SelectQuery<T> innerJoin(String table, String onCondition) {
        return this;
    }

    public SelectQuery<T> outerJoin(String table, String onCondition) {
        return this;
    }

    public SelectQuery<T> union(SelectQuery<T> other) {
        return this;
    }

    public SelectQuery<T> unionAll(SelectQuery<T> other) {
        return this;
    }


    public SelectQuery<T> intersect(SelectQuery<T> other) {
        return this;
    }
    public SelectQuery<T> except(SelectQuery<T> other) {
        return this;
    }

    public SelectQuery<T> distinct() {
        return this;
    }

   /* public SelectQuery<T> forUpdate() {
        return this;
    }

    public SelectQuery<T> forShare() {
        return this;
    }

    public SelectQuery<T> forKeyShare() {
        return this;
    }

    public SelectQuery<T> forNoKeyUpdate() {
        return this;
    }
    public SelectQuery<T> forNoWait() {
        return this;
    }
    public SelectQuery<T> forSkipLocked() {
        return this;
    }
    public SelectQuery<T> forUpdateWait(int seconds) {
        return this;
    }
    public SelectQuery<T> forUpdateSkipLocked() {
        return this;
    }
    public SelectQuery<T> forUpdateNoWait() {
        return this;
    }
    public SelectQuery<T> forUpdateNowait() {
        return this;
    }
    public SelectQuery<T> forUpdateSkipLocked(int seconds) {
        return this;
    }
    public SelectQuery<T> forUpdateWait(int seconds, boolean skipLocked) {
        return this;
    }
    public SelectQuery<T> forUpdateSkipLocked(int seconds, boolean skipLocked) {
        return this;
    }
    public SelectQuery<T> forUpdateNoWait(int seconds) {
        return this;
    }
    public SelectQuery<T> forUpdateNowait(int seconds) {
        return this;
    }*/

}
