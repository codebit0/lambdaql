package org.lambdaql.analyzer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 두 개의 인자를 가지는 연산
 * a > b → 좌측 피연산자 a, 우측 피연산자 b, 연산자 >
 * Binary Comparison Condition
 */
@Getter
@Accessors(fluent = true)
@ToString
@EqualsAndHashCode(callSuper = false)
public class BinaryCondition implements ConditionExpression {
    private final Object left;
    private final Object right;
    protected BinaryOperator operator;
    protected final BinaryOperator originOperator;

    public BinaryCondition(Object left, BinaryOperator operator, Object right) {
        this.left = left;
        this.operator = operator;
        this.originOperator = operator;
        this.right = right;
    }

    public static BinaryCondition of(Object field, BinaryOperator operator, Object value) {
        return new BinaryCondition(field, operator, value);
    }
}