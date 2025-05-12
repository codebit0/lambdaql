package org.lambdaql.analyzer.node;

import lombok.Getter;
import lombok.Setter;
import org.lambdaql.analyzer.label.LabelInfo;

import java.util.ArrayList;
import java.util.List;

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
            leaf.setParentGroup(this); // ⬅ 연결
            connectLeaf(leaf);         // 기존 B+Leaf 연결
        } else if (node instanceof ConditionGroupNode group) {
            group.setParent(this);
            connectLeafRange(group.getFirstLeaf(), group.getLastLeaf());
        }
    }

    private void connectLeaf(ConditionLeafNode leaf) {
        if (firstLeaf == null) {
            firstLeaf = leaf;
            lastLeaf = leaf;
        } else {
            lastLeaf.setNextLeaf(leaf);
            lastLeaf = leaf;
        }
    }

    private void connectLeafRange(ConditionLeafNode from, ConditionLeafNode to) {
        if (from == null || to == null) return;
        if (firstLeaf == null) {
            firstLeaf = from;
            lastLeaf = to;
        } else {
            lastLeaf.setNextLeaf(from);
            lastLeaf = to;
        }
    }

    public ConditionLeafNode getFirstLeaf() { return firstLeaf; }
    public ConditionLeafNode getLastLeaf() { return lastLeaf; }

    public List<ConditionNode> getChildren() { return children; }
    public LabelInfo getLabelInfo() { return labelInfo; }

    public List<ConditionLeafNode> leafs() {
        List<ConditionLeafNode> list = new ArrayList<>();
        ConditionLeafNode current = firstLeaf;
        while (current != null) {
            list.add(current);
            if (current == lastLeaf) break;
            current = current.getNextLeaf();
        }
        return list;
    }


    @Deprecated
    public void setLabelInfo(LabelInfo info) { this.labelInfo = info; }

    public List<ConditionNode> getSiblings() {
        List<ConditionNode> siblings = new ArrayList<>();
        if (parent == null) return siblings;

        for (ConditionNode node : parent.getChildren()) {
            if (node != this) {
                siblings.add(node);
            }
        }
        return siblings;
    }

    public List<ConditionNode> getSiblingsFrom(ConditionNode node, boolean remove) {
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
}


