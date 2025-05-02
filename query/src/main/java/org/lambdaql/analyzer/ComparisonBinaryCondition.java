package org.lambdaql.analyzer;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.lambdaql.analyzer.label.LabelInfo;

/**
 * 두 개의 인자를 가지는 연산
 * a > b → 좌측 피연산자 a, 우측 피연산자 b, 연산자 >
 * Binary Comparison Condition
 */
@Getter
@Accessors(fluent = true)
@ToString
public class ComparisonBinaryCondition extends BinaryCondition {

    private final LabelInfo labelInfo;

    public static ComparisonBinaryCondition of(Object field, BinaryOperator operator, Object value, LabelInfo labelInfo) {
        return new ComparisonBinaryCondition(field, operator, value, labelInfo);
    }

    public ComparisonBinaryCondition(Object field, BinaryOperator operator, Object value, LabelInfo labelInfo) {
        super(field, operator, value);
        this.labelInfo = labelInfo;
    }

    public ComparisonBinaryCondition reverseOperator() {
        this.operator = this.operator.not();
        return this;
    }
}