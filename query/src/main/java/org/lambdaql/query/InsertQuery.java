package org.lambdaql.query;

import org.lambdaql.analyzer.EntityColumnResolver;
import org.lambdaql.analyzer.LambdaFieldExtractor;
import org.lambdaql.analyzer.SerializableConsumer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class InsertQuery<T> {

    private final Class<T> entityType;
    private final Map<String, Object> fieldValues = new LinkedHashMap<>();

    public InsertQuery(Class<T> entityType, EntityColumnResolver columnResolver) {
        this.entityType = entityType;
    }

    public InsertQuery<T> values(SerializableConsumer<T> setter) {
        this.fieldValues.putAll(LambdaFieldExtractor.extractFields(entityType, setter));
        return this;
    }

    public void execute() {
        StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(entityType.getSimpleName())
                .append(" (")
                .append(String.join(", ", fieldValues.keySet()))
                .append(") VALUES (")
                .append(fieldValues.values().stream().map(this::formatValue).collect(Collectors.joining(", ")))
                .append(")");

        System.out.println("Executing SQL: " + sql);
    }

    private String formatValue(Object value) {
        return value instanceof String ? "'" + value + "'" : String.valueOf(value);
    }
}
