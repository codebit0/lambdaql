package org.lambdaql.analyzer.node;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.lambdaql.analyzer.ConditionExpression;
import org.lambdaql.analyzer.label.LabelInfo;

import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
public final class ConditionLeafNode implements ConditionNode {
    private final ConditionExpression condition;
    private final LabelInfo labelInfo;

    @Setter
    ConditionGroupNode group;

    public ConditionLeafNode(ConditionExpression condition, LabelInfo labelInfo) {
        this.condition = condition;
        this.labelInfo = labelInfo;
    }

    public List<ConditionNode> siblings() {
        List<ConditionNode> siblings = new ArrayList<>();

        for (ConditionNode node : group.children()) {
            if (node != this) {
                siblings.add(node);
            }
        }
        return siblings;
    }

    public String toString() {
        return "ConditionLeafNode{" +
                "labelInfo=" + labelInfo +
                ", condition=" + condition +
                '}';
    }
}

