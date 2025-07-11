package org.lambdaql.analyzer;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.lambdaql.analyzer.label.LabelInfo;

/**
 * !isActive 단항 조건
 */
@Getter
@Accessors(fluent = true)
public class UnaryCondition implements ConditionExpression {
    private final Object value;
    private final LabelInfo labelInfo;
    private final UnaryOperator originalOperator;
    private UnaryOperator operator;

    public static UnaryCondition of(Object value, UnaryOperator operator, LabelInfo labelInfo) {
        return new UnaryCondition(value, operator, labelInfo);
    }

    public UnaryCondition(Object value, UnaryOperator operator, LabelInfo labelInfo) {
        this.value = value;
        this.operator = operator;
        this.originalOperator = operator;
        this.labelInfo = labelInfo;
    }

    public UnaryCondition reverseOperator() {
        this.operator = this.operator.not();
        return this;
    }
}
