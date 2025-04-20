package org.lambdaql.analyzer;

/**
 * 두 개의 인자를 가지는 연산
 * a > b → 좌측 피연산자 a, 우측 피연산자 b, 연산자 >
 * Binary Comparison Condition
 */
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