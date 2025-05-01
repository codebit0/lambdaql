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
public class ComparisonBinaryCondition extends BinaryCondition {

    private final Object tureValue;
    private final Object falseValue;

    public static ComparisonBinaryCondition of(Object field, BinaryOperator operator, Object value, Object tureExpression, Object falseExpression) {
        return new ComparisonBinaryCondition(field, operator, value, tureExpression, falseExpression);
    }

    public ComparisonBinaryCondition(Object field, BinaryOperator operator, Object value, Object tureExpression, Object falseExpression) {
        super(field, operator, value);
        this.tureValue = tureExpression;
        this.falseValue = falseExpression;
    }

    public ComparisonBinaryCondition not() {
        return new ComparisonBinaryCondition(left(), operator().not(), right(), falseValue, tureValue);
    }
}