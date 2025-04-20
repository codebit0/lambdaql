package org.lambdaql.analyzer;

public class Condition {
    String field;
    String operator;
    Object value;
    String logical; // AND, OR, or null for first

    public Condition(String field, String operator, Object value, String logical) {
        this.field = field;
        this.operator = operator;
        this.value = value;
        this.logical = logical;
    }
}
