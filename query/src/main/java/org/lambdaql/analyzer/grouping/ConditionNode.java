package org.lambdaql.analyzer.grouping;

import org.lambdaql.analyzer.label.LabelInfo;

public sealed interface ConditionNode permits ConditionGroup, ConditionLeaf {
    LabelInfo labelInfo();
}
