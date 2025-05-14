package org.lambdaql.analyzer.node;

import org.lambdaql.analyzer.label.LabelInfo;

import java.util.ArrayList;
import java.util.List;

public class ConditionTreeBuilder {

    public ConditionGroupNode buildNestedTree(ConditionGroupNode root) {
        List<ConditionLeafNode> leafs = root.leafs();
        int idx = 0;
        for (ConditionLeafNode leaf : leafs) {
            LabelInfo info = leaf.labelInfo();
            if (info != null && info.value() == null) {
                ConditionGroupNode group = leaf.group();
                if (group.labelInfo() == info) {
                    //그룹의 첫번째와 현재 리프가 같다면 그이미 그룹핑 되어 있으므로 추가 그룹핑 하지 않음
                    boolean equals = group.firstLeaf().equals(leaf);
                    if(!equals) {
                        ConditionGroupNode newGroup = new ConditionGroupNode(info);
                        List<ConditionNode> siblings =group.siblingsFrom(leaf, true);
                        for (ConditionNode sibling : siblings) {
                            newGroup.addChild(sibling);
                        }
                        group.addChild(newGroup);
                    }
                } else  {
                    //자신의 그룹과 그룹핑되지 않는 다면 상위 그룹으로 이동하며 같은 라벨을 찾
//                    List<ConditionNode> siblings = group.siblings();
                    ConditionGroupNode cursor = group;
                    while (cursor != null && !cursor.isRoot()) {
                        List<ConditionNode> siblings = cursor.siblings();
                        for (ConditionNode sibling : siblings) {
                            if (sibling instanceof ConditionGroupNode siblingGroup &&
                                    info.equals(siblingGroup.labelInfo())) {
                                // 같은 라벨을 찾았다면 현재 그룹 부터 찾음 그룹까지를 그룹핑
                                List<ConditionNode> toWrap = cursor.siblingsFrom(leaf, true);
                                for (ConditionNode n : toWrap) {
                                    siblingGroup.addChild(n);
                                }
                                break;
                            }
                        }
                        cursor = cursor.parent();
                    }
                    System.out.println("cursor = " + cursor);
//                    System.out.println(siblings);
                    /*ConditionGroupNode cursor = group;
                    while (cursor != null && !cursor.isRoot()) {
                        List<ConditionNode> siblingCandidates = cursor.parent().siblingsFrom(cursor, false);
                        int matchIndex = -1;

                        for (int i = 0; i < siblingCandidates.size(); i++) {
                            ConditionNode sibling = siblingCandidates.get(i);
                            if (sibling instanceof ConditionGroupNode siblingGroup &&
                                    info.equals(siblingGroup.labelInfo())) {
                                matchIndex = i;
                                break;
                            }
                        }

                        if (matchIndex != -1) {
                            List<ConditionNode> toWrap = siblingCandidates.subList(0, matchIndex + 1);
                            ConditionGroupNode newGroup = new ConditionGroupNode(info);
                            for (ConditionNode n : toWrap) {
                                newGroup.addChild(n);
                            }

                            // 부모 갱신
                            ConditionGroupNode parent = cursor.parent();
                            List<ConditionNode> updated = new ArrayList<>(parent.children());
                            int startIndex = updated.indexOf(toWrap.get(0));
                            updated.removeAll(toWrap);
                            updated.add(startIndex, newGroup);

                            parent.children().clear();
                            for (ConditionNode n : updated) {
                                parent.addChild(n);
                            }

                            break;
                        }

                        cursor = cursor.parent();
                    }*/
                }
            }
        }
        return root;
    }
}
