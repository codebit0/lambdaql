package org.lambdaql.analyzer;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * 두 개의 인자를 가지는 연산
 * a > b → 좌측 피연산자 a, 우측 피연산자 b, 연산자 >
 * Binary Comparison Condition
 */
@Getter
@Accessors(fluent = true)
public class BinaryCondition extends ConditionExpression {
    private final Object field;
    private final BinaryOperator operator;
    private final Object value;

    public BinaryCondition(Object field, BinaryOperator operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public static BinaryCondition of(Object field, BinaryOperator operator, Object value) {
        return new BinaryCondition(field, operator, value);
    }
}