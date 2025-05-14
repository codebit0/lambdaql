package org.lambdaql.analyzer.node;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.lambdaql.analyzer.label.LabelInfo;

import java.util.ArrayList;
import java.util.List;

@Accessors(fluent = true)
public final class ConditionGroupNode implements ConditionNode {
    private final List<ConditionNode> children = new ArrayList<>();
    private LabelInfo labelInfo;
    private ConditionLeafNode firstLeaf;
    private ConditionLeafNode lastLeaf;

    private static final ConditionGroupNode ROOT = new ConditionGroupNode();

    private ConditionGroupNode(){
    }

    public ConditionGroupNode(LabelInfo labelInfo) {
        this.labelInfo = labelInfo;
    }

    @Getter
    @Setter
    private ConditionGroupNode parent = ROOT;

    public void addChild(ConditionNode node) {
        children.add(node);
        if (node instanceof ConditionLeafNode leaf) {
            leaf.group(this); // ⬅ 연결
            connectLeaf(leaf);         // 기존 B+Leaf 연결
        } else if (node instanceof ConditionGroupNode group) {
            group.parent(this);
            connectLeafRange(group.firstLeaf(), group.lastLeaf());
        }
    }

    private void connectLeaf(ConditionLeafNode leaf) {
        if (firstLeaf == null) {
            firstLeaf = leaf;
            lastLeaf = leaf;
        } else {
            lastLeaf.nextLeaf(leaf);
            lastLeaf = leaf;
        }
    }

    private void connectLeafRange(ConditionLeafNode from, ConditionLeafNode to) {
        if (from == null || to == null) return;
        if (firstLeaf == null) {
            firstLeaf = from;
        } else {
            lastLeaf.nextLeaf(from);
        }
        lastLeaf = to;
    }

    public ConditionLeafNode firstLeaf() { return firstLeaf; }
    public ConditionLeafNode lastLeaf() { return lastLeaf; }

    public List<ConditionNode> children() { return children; }
    public LabelInfo labelInfo() { return labelInfo; }

    public List<ConditionLeafNode> leafs() {
        List<ConditionLeafNode> list = new ArrayList<>();
        ConditionLeafNode current = firstLeaf;
        while (current != null) {
            list.add(current);
            if (current == lastLeaf) break;
            current = current.nextLeaf();
        }
        return list;
    }

    public List<ConditionNode> siblings() {
        List<ConditionNode> siblings = new ArrayList<>();
        if (parent == null) return siblings;

        for (ConditionNode node : parent.children()) {
            if (node != this) {
                siblings.add(node);
            }
        }
        return siblings;
    }

    public List<ConditionNode> siblingsFrom(ConditionNode node, boolean remove) {
        List<ConditionNode> siblings = new ArrayList<>();
        int index = children.indexOf(node);
        if (index == -1) return siblings;

        for (int i = index; i < children.size(); i++) {
            siblings.add(children.get(i));
        }

        if (remove) {
            // 주의: 한 번에 삭제해야 인덱스 꼬임 없음
            children.subList(index, children.size()).clear();
        }

        return siblings;
    }



    public boolean isRoot() {
        return parent == ROOT;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConditionGroupNode{");
        sb.append("labelInfo=").append(labelInfo);
        sb.append(", isRoot=").append(isRoot());
        sb.append('}');
        return sb.toString();
    }
}


