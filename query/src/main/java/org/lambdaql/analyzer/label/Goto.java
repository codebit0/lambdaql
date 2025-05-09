package org.lambdaql.analyzer.label;

import org.lambdaql.analyzer.ConditionExpression;

public record Goto(LabelInfo labelInfo) implements ConditionExpression {
    public static Goto of(LabelInfo labelInfo) {
        return new Goto(labelInfo);
    }

    public Goto(LabelInfo labelInfo) {
        this.labelInfo = labelInfo;
    }
}
