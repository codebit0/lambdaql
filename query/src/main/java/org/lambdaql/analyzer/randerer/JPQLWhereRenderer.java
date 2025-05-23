package org.lambdaql.analyzer.randerer;

import lombok.Setter;
import org.lambdaql.analyzer.*;
import org.lambdaql.analyzer.grouping.ConditionGroup;
import org.lambdaql.analyzer.grouping.ConditionLeaf;
import org.lambdaql.analyzer.grouping.ConditionNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JPQLWhereRenderer {

    private static final String OPEN_BRACKET = "(";
    private static final String CLOSE_BRACKET = ")";

    @Setter
    private LambdaVariableAnalyzer lambdaVariable;

    public JPQLWhereRenderer(ConditionGroup root) {
        Map<String, Object> params = new HashMap<>();
        List<Object> flattened = flattenGroups(root, 0, params);
    }


    private List<Object> flattenGroups(ConditionGroup group, int depth, Map<String, Object> params) {
        List<Object> results = new ArrayList<>();
        List<ConditionNode> children = group.children();
        int size = children.size();
        boolean isRoot = group.isRoot();
        if (!isRoot) {
            results.add(OPEN_BRACKET);
        }
        for (int i = 0; i < size; i++) {
            ConditionNode child = children.get(i);
            if (child instanceof ConditionGroup g) {
                List<Object> list = flattenGroups(g, depth + 1, params);
                results.addAll(list);
            } else if (child instanceof ConditionLeaf l) {
                ConditionExpression expr = l.condition();
                if (expr instanceof ComparisonBinaryCondition binary) {
                    Object left = binary.left();
                    Object right = binary.right();
                    if (left instanceof ObjectCapturedVariable variable) {
                        String paramName = ":param" + variable.sequenceIndex();
                        left = ":"+paramName;
                        params.put(paramName, findCaptureVarInsn(variable.sequenceIndex()));
                    }
                    results.add(left);
                    results.add(binary.operator().symbol());
                    results.add(right);
                    if(!isRoot && i == size -1) {
                        //depth 만큼 닫기 괄호 추가
                        results.add(CLOSE_BRACKET.repeat(depth));
                    }
                    if(!isRoot)
                        results.add(binary.logicalOperator().name());
                }
            }

        }
        return results;
    }


    private ICapturedVariable findCaptureVarInsn(int varIndex) {
        return lambdaVariable.getCapturedValueOpcodeIndex(varIndex);
    }
}
