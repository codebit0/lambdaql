package org.lambdaql.analyzer;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Accessors(fluent = true)
public class LogicalCondition extends ConditionExpression {
    public final LogicalOperator operator; // AND / OR
    public final List<ConditionExpression> conditions;

    public LogicalCondition(LogicalOperator operator, List<ConditionExpression> conditions) {
        this.operator = operator;
        this.conditions = conditions;
    }
}