package org.lambdaql.analyzer.node;

import org.lambdaql.analyzer.label.LabelInfo;

public sealed interface ConditionNode permits ConditionGroupNode, ConditionLeafNode {
    LabelInfo labelInfo();
}
