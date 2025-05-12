package org.lambdaql.analyzer.node;

import lombok.Getter;
import lombok.Setter;
import org.lambdaql.analyzer.ConditionExpression;
import org.lambdaql.analyzer.label.LabelInfo;

import java.util.ArrayList;
import java.util.List;

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

    public List<ConditionNode> getSiblings() {
        List<ConditionNode> siblings = new ArrayList<>();

        for (ConditionNode node : parentGroup.getChildren()) {
            if (node != this) {
                siblings.add(node);
            }
        }
        return siblings;
    }

    public void setNextLeaf(ConditionLeafNode next) { this.nextLeaf = next; }

    public ConditionLeafNode getNextLeaf() { return nextLeaf; }


}

