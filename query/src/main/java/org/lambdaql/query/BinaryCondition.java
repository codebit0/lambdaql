package org.lambdaql.query;

public class BinaryCondition extends ConditionExpr {
    public final String field;
    public final String operator;
    public final Object value;

    public BinaryCondition(String field, String operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

}