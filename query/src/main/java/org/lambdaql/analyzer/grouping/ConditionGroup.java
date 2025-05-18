package org.lambdaql.analyzer.grouping;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.lambdaql.analyzer.label.LabelInfo;

import java.util.*;
import java.util.stream.Stream;

@Accessors(fluent = true)
public final class ConditionGroup implements ConditionNode {
    @Getter
    private final List<ConditionNode> children = new ArrayList<>();

    @Getter
    private LabelInfo labelInfo;

    @Getter
    private ConditionGroup parent = null;

    @Getter
    private boolean isRoot = false;

    private List<ConditionLeaf> rootLeafs = new ArrayList<>();


    public static ConditionGroup root(LabelInfo labelInfo) {
        return new ConditionGroup(labelInfo, true);
    }

    public static void makeGrouping(ConditionGroup root) {
        List<ConditionLeaf> leafs = root.leafs();
        for (ConditionLeaf leaf : leafs) {
            LabelInfo info = leaf.labelInfo();
            if (info != null && info.value() == null) {
                ConditionGroup group = leaf.group();
                if (group.labelInfo() == info) {
                    group.grouping(leaf);
                } else  {
                    //자신의 그룹과 그룹핑되지 않는 다면 상위 그룹으로 이동하며 같은 라벨을 찾음
                    ConditionGroup cursor = group;
                    searchLoop: while (cursor != null) {
                        List<ConditionNode> siblings = cursor.siblings();
                        for (ConditionNode sibling : siblings) {
                            if (sibling instanceof ConditionGroup siblingGroup &&
                                    info.equals(siblingGroup.labelInfo())) {
                                // 같은 라벨을 찾았다면 현재 그룹 부터 찾음 그룹까지를 그룹핑
                                ConditionGroup parent = cursor.parent();
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

    private ConditionGroup(LabelInfo labelInfo, boolean isRoot) {
        this.labelInfo = labelInfo;
        this.isRoot = isRoot;
    }

    public ConditionGroup(LabelInfo labelInfo) {
        this.labelInfo = labelInfo;
    }

    public void addChild(ConditionNode node) {
        children.add(node);
        if (node instanceof ConditionLeaf leaf) {
            leaf.group = this;
            if(isRoot) {
                rootLeafs.add(leaf);
            }
        } else if (node instanceof ConditionGroup group) {
            group.parent =this;
            if(isRoot) {
                rootLeafs.addAll(group.leafs());
            }
        }
    }

    public List<ConditionLeaf> leafs() {
        if (isRoot) {
            return rootLeafs;
        }
        List<ConditionLeaf> list = new ArrayList<>();
        this.children.forEach(node -> {
            if (node instanceof ConditionLeaf leaf) {
                list.add(leaf);
            } else if (node instanceof ConditionGroup group) {
                list.addAll(group.leafs());
            }
        });
        return list;
    }

    public Stream<ConditionLeaf> leafStream() {
        if (isRoot) {
            return rootLeafs.stream();
        }
        return this.children.stream().flatMap(node -> {
            if (node instanceof ConditionLeaf leaf) {
                return Stream.of(leaf);
            } else if (node instanceof ConditionGroup group) {
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

    public ConditionGroup grouping(ConditionNode start) {
        int startIndex = children.indexOf(start);
        if (startIndex == 0)
            return this;
        if (startIndex == -1)
            throw new IllegalArgumentException("Invalid range for grouping");

        List<ConditionNode> range = new ArrayList<>(children.subList(startIndex, children.size()));
        ConditionGroup newGroup = new ConditionGroup(range.getFirst().labelInfo());
        newGroup.parent = this;
        for (ConditionNode node : range) {
            newGroup.addChild(node); // skipLeafConnect = true
        }

        children.subList(startIndex, children.size()).clear();
        children.add(startIndex, newGroup);

        return newGroup;
    }

    public ConditionGroup grouping(ConditionNode start, ConditionNode end) {
        int startIndex = children.indexOf(start);
        int endIndex = children.indexOf(end);
        if (startIndex == 0 && endIndex == children.size() - 1)
            return this;
        if (startIndex == -1 || endIndex == -1 || startIndex > endIndex)
            throw new IllegalArgumentException("Invalid range for grouping");

        List<ConditionNode> range = new ArrayList<>(children.subList(startIndex, endIndex + 1));
        ConditionGroup newGroup = new ConditionGroup(range.getLast().labelInfo());
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
        sb.append("ConditionGroup{");
        sb.append("labelInfo=").append(labelInfo);
        sb.append(", isRoot=").append(isRoot());
        sb.append('}');
        return sb.toString();
    }
}


