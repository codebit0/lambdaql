package org.lambdaql.analyzer.node;

import org.lambdaql.analyzer.label.LabelInfo;

import java.util.ArrayList;
import java.util.List;

public class ConditionTreeBuilder {

    public ConditionGroupNode buildNestedTree(ConditionGroupNode root) {
        List<ConditionLeafNode> leafs = root.leafs();
        int idx = 0;
        for (ConditionLeafNode leaf : leafs) {
            if(idx == 0) {
                idx++;
                continue;
            }
            idx++;

            LabelInfo info = leaf.getLabelInfo();
            if (info != null && info.value() == null) {
                ConditionGroupNode group = leaf.getGroup();
                if (group.getLabelInfo() == info) {
                    //자신의 그룹에서 그룹핑이 된 경우
                    //FIXME 다만 자신의 그룹에서 첫번째인 경우는 제외
                    ConditionGroupNode newGroup = new ConditionGroupNode(info);
                    List<ConditionNode> siblings =group.getSiblingsFrom(leaf, true);
                    for (ConditionNode sibling : siblings) {
                        newGroup.addChild(sibling);
                    }
                    group.addChild(newGroup);
                }
            }
        }
//        ConditionLeafNode current = root.getFirstLeaf();
//        int idx = 1;
//        while (current != null) {
//            current = current.getNextLeaf();
//            LabelInfo info = current.getLabelInfo();
//            if (info != null && info.value() == null) {
//                ConditionGroupNode group = current.getParentGroup();
//                if(group.getLabelInfo() == info) {
//                    //자신의 그룹에서 그룹핑이 된 경우
//                    ConditionGroupNode newGroup = new ConditionGroupNode(info);
//
////                    List<ConditionNode> siblings =group.getSiblingsFrom(current, true);
////                    for (ConditionNode sibling : siblings) {
////                        newGroup.addChild(sibling);
////                    }
////                    group.addChild(newGroup);
//                }
//
//
//            }
//
//            idx++;
//        }
        return root;
    }

    public List<ConditionGroupNode> buildNestedTree2(List<ConditionGroupNode> groups) {
        int i = 1;
        while (i < groups.size()) {
            ConditionGroupNode current = groups.get(i);
            boolean merged = false;

            for (ConditionNode node : current.getChildren()) {
                if (node instanceof ConditionLeafNode leaf) {
                    LabelInfo info = leaf.getLabelInfo();
                    if (info != null && info.value() == null) {
                        int matchIndex = findGroupIndexWithLabelDeep(groups, i + 1, info);
                        if (matchIndex != -1) {
                            ConditionGroupNode newGroup = new ConditionGroupNode(info);
                            for (int k = i; k <= matchIndex; k++) {
                                newGroup.addChild(groups.get(k));
                            }
                            List<ConditionGroupNode> updated = new ArrayList<>(groups.subList(0, i));
                            updated.add(newGroup);
                            if (matchIndex + 1 < groups.size()) {
                                updated.addAll(groups.subList(matchIndex + 1, groups.size()));
                            }
                            groups = updated;
                            merged = true;
                            break; // 현재 i에서 다시 검사
                        }
                    }
                }
            }

            if (!merged) {
                i++;
            }
        }
        return groups;
    }


    public List<ConditionGroupNode> buildNestedTree5(List<ConditionGroupNode> flatGroups) {
        int i = 1;
        while (i < flatGroups.size()) {
            ConditionGroupNode currentGroup = flatGroups.get(i);
            boolean merged = false;

            for (ConditionNode node : currentGroup.getChildren()) {
                if (node instanceof ConditionLeafNode leaf) {
                    LabelInfo info = leaf.getLabelInfo();
                    if (info != null && info.value() == null) {

                        // 뒤쪽에서 동일한 라벨을 가진 그룹 탐색
                        int matchEnd = findGroupWithLabel(flatGroups, info, i + 1);
                        if (matchEnd != -1) {
                            // i부터 matchEnd까지 묶어서 새로운 그룹 생성
                            ConditionGroupNode newGroup = new ConditionGroupNode(info);
                            for (int k = i; k <= matchEnd; k++) {
                                newGroup.addChild(flatGroups.get(k));
                            }

                            // 치환
                            List<ConditionGroupNode> updated = new ArrayList<>(flatGroups.subList(0, i));
                            updated.add(newGroup);
                            if (matchEnd + 1 < flatGroups.size()) {
                                updated.addAll(flatGroups.subList(matchEnd + 1, flatGroups.size()));
                            }

                            flatGroups = updated;
                            merged = true;
                            break; // 다시 현재 위치에서 반복
                        }
                    }
                }
            }

            if (!merged) {
                i++;
            }
        }

        return flatGroups;
    }

    private int findGroupWithLabel(List<ConditionGroupNode> groups, LabelInfo label, int startIndex) {
        for (int j = startIndex; j < groups.size(); j++) {
            ConditionGroupNode g = groups.get(j);
            LabelInfo info = g.getLabelInfo();
            if (label.equals(info)) {
                return j;
            }
        }
        return -1;
    }

    // 트리 내부까지 탐색하여 labelInfo.label 에 해당하는 그룹 인덱스를 반환
    private int findGroupIndexWithLabelDeep(List<ConditionGroupNode> groups, int start, LabelInfo label) {
        for (int i = start; i < groups.size(); i++) {
            if (containsLabelDeep(groups.get(i), label)) {
                return i;
            }
        }
        return -1;
    }

    private boolean containsLabelDeep(ConditionGroupNode group, LabelInfo label) {
        if (group.getLabelInfo() != null && label.equals(group.getLabelInfo())) {
            return true;
        }
        for (ConditionNode node : group.getChildren()) {
            if (node instanceof ConditionLeafNode leaf) {
                if (label.equals(leaf.getLabelInfo())) {
                    return true;
                }
            } else if (node instanceof ConditionGroupNode childGroup) {
                if (containsLabelDeep(childGroup, label)) {
                    return true;
                }
            }
        }
        return false;
    }


    public List<ConditionGroupNode> buildNestedTree3(List<ConditionGroupNode> rootGroups) {
        for (ConditionGroupNode group : rootGroups) {
            visitGroup(group);
        }
        return rootGroups;
    }

    private void visitGroup(ConditionGroupNode parentGroup) {
        List<ConditionNode> children = new ArrayList<>(parentGroup.getChildren());
        int i = 0;
        while (i < children.size()) {
            ConditionNode current = children.get(i);

            if (current instanceof ConditionGroupNode currentGroup) {
                visitGroup(currentGroup);

                LabelInfo targetLabel = null;
                for (ConditionNode child : currentGroup.getChildren()) {
                    if (child instanceof ConditionLeafNode leaf) {
                        LabelInfo labelInfo = leaf.getLabelInfo();
                        if (labelInfo != null && labelInfo.value() == null) {
                            targetLabel = labelInfo;
                            break;
                        }
                    }
                }

                if (targetLabel != null) {
                    int matchIndex = findGroupIndexWithLabelAmongSiblings(children, targetLabel, i + 1);
                    if (matchIndex != -1) {
                        ConditionGroupNode newGroup = new ConditionGroupNode(targetLabel);
                        for (int k = i; k <= matchIndex; k++) {
                            newGroup.addChild(children.get(k));
                        }

                        List<ConditionNode> updated = new ArrayList<>(children.subList(0, i));
                        updated.add(newGroup);
                        if (matchIndex + 1 < children.size()) {
                            updated.addAll(children.subList(matchIndex + 1, children.size()));
                        }

                        parentGroup.getChildren().clear();
                        for (ConditionNode node : updated) {
                            parentGroup.addChild(node);
                        }

                        children = new ArrayList<>(parentGroup.getChildren());
                        continue;
                    }
                }
            }
            i++;
        }
    }

    private int findGroupIndexWithLabelAmongSiblings(List<ConditionNode> siblings, LabelInfo labelInfo, int start) {
        for (int i = start; i < siblings.size(); i++) {
            ConditionNode node = siblings.get(i);
            if (node instanceof ConditionGroupNode group && labelInfo.equals(group.getLabelInfo())) {
                return i;
            }
        }
        return -1;
    }
}
