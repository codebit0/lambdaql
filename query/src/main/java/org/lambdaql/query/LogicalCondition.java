package org.lambdaql.query;

import java.util.List;

public class LogicalCondition extends ConditionExpr {
    public final String operator; // AND / OR
    public final List<ConditionExpr> conditions;

    public LogicalCondition(String operator, List<ConditionExpr> conditions) {
        this.operator = operator;
        this.conditions = conditions;
    }
}