package org.lambdaql.analyzer.label;

import org.lambdaql.analyzer.ConditionExpression;

public record Return(LabelInfo labelInfo) implements ConditionExpression {
    public static Return of(LabelInfo labelInfo) {
        return new Return(labelInfo);
    }

    public Return(LabelInfo labelInfo) {
        this.labelInfo = labelInfo;
    }
}
