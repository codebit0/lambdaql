package org.lambdaql.query;

import org.lambdaql.analyzer.Condition;
import org.lambdaql.analyzer.EntityColumnResolver;
import org.lambdaql.analyzer.SerializableConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UpdateQuery<T> {
    private final Class<T> entityClass;
    private final EntityColumnResolver columnResolver;
    private final List<SetClause> sets = new ArrayList<>();
    private final List<Condition> whereConditions = new ArrayList<>();

    public UpdateQuery(Class<T> entityClass, EntityColumnResolver columnResolver) {
        this.entityClass = entityClass;
        this.columnResolver = columnResolver;
    }

    public UpdateQuery<T> set(SerializableConsumer<T> lambda) {
        //this.updates.putAll(LambdaFieldExtractor.extractFields(entityType, setter));
        return this;
    }

    public UpdateQuery<T> where(Predicate<T> lambda) {
        // 람다 파싱해서 whereConditions 리스트에 추가
        return this;
    }

    public void execute() {
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(entityClass.getSimpleName()).append(" SET ");

        sql.append(sets.stream()
                .map(s -> columnResolver.resolveColumnName(entityClass, s.getField())
                        + " = " + formatValue(s.getValue()))
                .collect(Collectors.joining(", ")));

        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ");
            /*sql.append(whereConditions.stream()
                    .map(c -> (c.getLogical() != null ? c.getLogical() + " " : "") +
                            columnResolver.resolveColumnName(entityClass, c.getField()) +
                            " " + c.getOperator() + " " + formatValue(c.getValue()))
                    .collect(Collectors.joining(" ")));*/
        }

        System.out.println("Executing SQL: " + sql);
    }

    private String formatValue(Object value) {
        return value instanceof String ? "'" + value + "'" : String.valueOf(value);
    }
}
