package org.lambdaql.query;

import org.lambdaql.analyzer.ConditionExpression;
import org.lambdaql.analyzer.LambdaWhereAnalyzer;
import org.lambdaql.analyzer.Renderer;

import java.io.Serializable;
import java.util.List;

public class SelectQuery<T> {

    private final Class<T> entityClass;
    protected final QueryBuilder queryBuilder;
    private ConditionExpression condition;

    @FunctionalInterface
    public interface Where<U> extends Serializable {
        boolean clause(U obj) throws Exception;
    }

    @FunctionalInterface
    public interface JoinOn<T,U> extends Serializable {
        boolean on(T t, U join);
    }

    @FunctionalInterface
    public interface Select<T,R> extends Serializable {
        R clause(T entity);
    }

    public static class SelectWhere<T> {
        public SelectWhere(SelectQuery<T> selectQuery) {

        }

        public SelectWhere<T> and(Where<T> condition) {
            return this;
        }

        public SelectWhere<T> or(Where<T> condition) {
            return this;
        }
    }

    public SelectQuery(QueryBuilder queryBuilder, Class<T> entityClass) {
        this.entityClass = entityClass;
        this.queryBuilder = queryBuilder;
    }

    public <R> SelectQuery<T> select(Select<T, R> clause) {
        return this;
    }

    public SelectWhere<T> where(Where<T> condition) {
        this.condition = new LambdaWhereAnalyzer(queryBuilder, List.of(entityClass)).analyze(condition);
        return new SelectWhere<T>(this);
    }

    public <J> SelectJoinQuery<T, J> join(Class<J> joinEntity, SelectJoinQuery.JoinOn<T, J> onCondition) {
        return new SelectJoinQuery<T, J>(this, joinEntity,  onCondition);
    }

    public <J> SelectLeftJoinQuery<T, J> leftJoin(Class<J> joinEntity, SelectLeftJoinQuery.JoinOn<T, J> onCondition) {
        return new SelectLeftJoinQuery<T, J>(this, joinEntity,  onCondition);
    }


    public SelectQuery<T> limit(int limit) {
        return this;
    }

    public SelectQuery<T> offset(int offset) {
        return this;
    }

    public SelectQuery<T> groupBy(String field) {
        return this;
    }

    public SelectQuery<T> having(ConditionExpression condition) {
        return this;
    }

    public String toSql() {
        if (condition == null) return "";
        return "SELECT * FROM " + entityClass.getSimpleName().toLowerCase() +
                " WHERE "+ Renderer.toSql("");
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
