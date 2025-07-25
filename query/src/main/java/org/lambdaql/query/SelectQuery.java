package org.lambdaql.query;

import org.lambdaql.analyzer.ConditionExpression;
import org.lambdaql.analyzer.LambdaWhereAnalyzer;
import org.lambdaql.analyzer.Renderer;
import org.lambdaql.analyzer.grouping.ConditionGroup;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

public class SelectQuery<T> {

    private final Class<T> entityClass;
    protected final QueryBuilder queryBuilder;
    private ConditionGroup condition;

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

    public static class SelectWhere<T> extends SelectQuery<T> {
        public SelectWhere(SelectQuery<T> selectQuery) {
            super(selectQuery.queryBuilder, selectQuery.entityClass);
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

    public <J> SelectRightJoinQuery<T, J> rightJoin(Class<J> joinEntity, SelectRightJoinQuery.JoinOn<T, J> onCondition) {
        return new SelectRightJoinQuery<T, J>(this, joinEntity,  onCondition);
    }

    public <J> SelectCrossJoinQuery<T, J> crossJoin(Class<J> joinEntity, SelectCrossJoinQuery.JoinOn<T, J> onCondition) {
        return new SelectCrossJoinQuery<T, J>(this, joinEntity,  onCondition);
    }

//    @FunctionalInterface
//    public interface OrderBy2<T> extends Serializable {
//        List<Supplier<T>> clause(T entity);
//    }

    @FunctionalInterface
    public interface Order<T> extends Serializable {
        List<OrderBy2> direction(T entity);
    }

    public interface OrderClause<T> {
        OrderClause<T> orderBy(Function<T, ?> fieldGetter);
        OrderClause<T> orderBy(Function<T, ?> fieldGetter, OrderBy direction);
        <R> R select(Function<T, R> selector);

//        default SelectQuery<T> limit(int limit) {
//            return this;
//        }
//
//        default SelectQuery<T> offset(int offset) {
//            return this;
//        }
    }


//    public interface OrderBy2<T> extends Serializable {
//        public static void asc(Supplier<?> supplier) {
//
//        }
//
//        public static void desc(Supplier<?> supplier) {
//
//        }
//    }
//
////    @FunctionalInterface
//    public interface Desc<T> extends OrderBy2<T>, Serializable {
//
//    }
//
////    @FunctionalInterface
//    public interface Asc<T> extends OrderBy2<T>, Serializable {
//
//    }

//
//
//    public SelectQuery<T> orderBy(OrderBy2<T> orderBy, OrderBy2<T> ... othderOrderBy) {
//        return this;
//    }

//    public SelectQuery<T> orderBy(Order<T> orderBy) {
//        return this;
//    }

//    public SelectQuery<T> orderBy(Function<T, ?> fieldGetter) {
//        return this;
//    }


//    public SelectQuery<T> orderBy(Function<T, ?> fieldGetter, OrderBy direction) {
//        return this;
//    }

    public SelectQuery<T> orderBy(Function<T, ?> fieldGetter, boolean descending=false) {
        return this;
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
