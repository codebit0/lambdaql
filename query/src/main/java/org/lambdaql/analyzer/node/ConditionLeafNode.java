package org.lambdaql.analyzer.node;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.lambdaql.analyzer.ConditionExpression;
import org.lambdaql.analyzer.label.LabelInfo;

@Getter
public final class ConditionLeafNode implements ConditionNode {
    private final ConditionExpression condition;
    private final LabelInfo labelInfo;
    private ConditionLeafNode nextLeaf;

    @Setter
    private ConditionGroupNode parentGroup;

    public ConditionLeafNode(ConditionExpression condition, LabelInfo labelInfo) {
        this.condition = condition;
        this.labelInfo = labelInfo;
    }

    public void setNextLeaf(ConditionLeafNode next) { this.nextLeaf = next; }

    public ConditionLeafNode getNextLeaf() { return nextLeaf; }
}

