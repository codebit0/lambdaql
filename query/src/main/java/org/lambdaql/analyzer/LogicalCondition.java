package org.lambdaql.analyzer;

import java.util.List;

public class LogicalCondition extends ConditionExpr {
    public final LogicalOperator operator; // AND / OR
    public final List<ConditionExpr> conditions;

    public LogicalCondition(LogicalOperator operator, List<ConditionExpr> conditions) {
        this.operator = operator;
        this.conditions = conditions;
    }
}