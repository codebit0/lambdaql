package org.lambdaql.query;

public class SetClause {
    private final String field;
    private final Object value;

    public SetClause(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }
}

