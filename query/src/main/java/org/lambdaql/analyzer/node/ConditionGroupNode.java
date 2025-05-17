package org.lambdaql.analyzer.node;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.lambdaql.analyzer.label.LabelInfo;

import java.util.*;
import java.util.stream.Stream;

@Accessors(fluent = true)
public final class ConditionGroupNode implements ConditionNode {
    @Getter
    private final List<ConditionNode> children = new ArrayList<>();

    @Getter
    private LabelInfo labelInfo;

    @Getter
    private ConditionGroupNode parent = null;

    @Getter
    private boolean isRoot = false;

    private List<ConditionLeafNode> rootLeafs = new ArrayList<>();


    public static ConditionGroupNode root(LabelInfo labelInfo) {
        return new ConditionGroupNode(labelInfo, true);
    }

    public static void makeGrouping(ConditionGroupNode root) {
        List<ConditionLeafNode> leafs = root.leafs();
        for (ConditionLeafNode leaf : leafs) {
            LabelInfo info = leaf.labelInfo();
            if (info != null && info.value() == null) {
                ConditionGroupNode group = leaf.group();
                if (group.labelInfo() == info) {
                    group.grouping(leaf);
                } else  {
                    //자신의 그룹과 그룹핑되지 않는 다면 상위 그룹으로 이동하며 같은 라벨을 찾음
                    ConditionGroupNode cursor = group;
                    searchLoop: while (cursor != null) {
                        List<ConditionNode> siblings = cursor.siblings();
                        for (ConditionNode sibling : siblings) {
                            if (sibling instanceof ConditionGroupNode siblingGroup &&
                                    info.equals(siblingGroup.labelInfo())) {
                                // 같은 라벨을 찾았다면 현재 그룹 부터 찾음 그룹까지를 그룹핑
                                ConditionGroupNode parent = cursor.parent();
                                parent.grouping(cursor, siblingGroup);
                                break searchLoop;
                            }
                        }
                        cursor = cursor.parent();
                    }
                }
            }
        }
    }

    private ConditionGroupNode(LabelInfo labelInfo, boolean isRoot) {
        this.labelInfo = labelInfo;
        this.isRoot = isRoot;
    }

    public ConditionGroupNode(LabelInfo labelInfo) {
        this.labelInfo = labelInfo;
    }

    public void addChild(ConditionNode node) {
        children.add(node);
        if (node instanceof ConditionLeafNode leaf) {
            leaf.group = this;
            if(isRoot) {
                rootLeafs.add(leaf);
            }
        } else if (node instanceof ConditionGroupNode group) {
            group.parent =this;
            if(isRoot) {
                rootLeafs.addAll(group.leafs());
            }
        }
    }

    public List<ConditionLeafNode> leafs() {
        if (isRoot) {
            return rootLeafs;
        }
        List<ConditionLeafNode> list = new ArrayList<>();
        this.children.forEach(node -> {
            if (node instanceof ConditionLeafNode leaf) {
                list.add(leaf);
            } else if (node instanceof ConditionGroupNode group) {
                list.addAll(group.leafs());
            }
        });
        return list;
    }

    public Stream<ConditionLeafNode> leafStream() {
        if (isRoot) {
            return rootLeafs.stream();
        }
        return this.children.stream().flatMap(node -> {
            if (node instanceof ConditionLeafNode leaf) {
                return Stream.of(leaf);
            } else if (node instanceof ConditionGroupNode group) {
                return group.leafStream();
            }
            return Stream.empty();
        });
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

    public ConditionGroupNode grouping(ConditionNode start) {
        int startIndex = children.indexOf(start);
        if (startIndex == 0)
            return this;
        if (startIndex == -1)
            throw new IllegalArgumentException("Invalid range for grouping");

        List<ConditionNode> range = new ArrayList<>(children.subList(startIndex, children.size()));
        ConditionGroupNode newGroup = new ConditionGroupNode(range.getFirst().labelInfo());
        newGroup.parent = this;
        for (ConditionNode node : range) {
            newGroup.addChild(node); // skipLeafConnect = true
        }

        children.subList(startIndex, children.size()).clear();
        children.add(startIndex, newGroup);

        return newGroup;
    }

    public ConditionGroupNode grouping(ConditionNode start, ConditionNode end) {
        int startIndex = children.indexOf(start);
        int endIndex = children.indexOf(end);
        if (startIndex == 0 && endIndex == children.size() - 1)
            return this;
        if (startIndex == -1 || endIndex == -1 || startIndex > endIndex)
            throw new IllegalArgumentException("Invalid range for grouping");

        List<ConditionNode> range = new ArrayList<>(children.subList(startIndex, endIndex + 1));
        ConditionGroupNode newGroup = new ConditionGroupNode(range.getLast().labelInfo());
        newGroup.parent = this;
        for (ConditionNode node : range) {
            newGroup.addChild(node); // skipLeafConnect = true
        }

        children.subList(startIndex, endIndex + 1).clear();
        children.add(startIndex, newGroup);

        return newGroup;
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


