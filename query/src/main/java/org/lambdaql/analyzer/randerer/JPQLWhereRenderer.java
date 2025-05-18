package org.lambdaql.analyzer.randerer;

import org.lambdaql.analyzer.*;
import org.lambdaql.analyzer.grouping.ConditionGroup;
import org.lambdaql.analyzer.grouping.ConditionLeaf;
import org.lambdaql.analyzer.grouping.ConditionNode;

import java.util.ArrayList;
import java.util.List;

public class JPQLWhereRenderer {

    public String render(ConditionGroup root) {
        StringBuilder builder = renderGroup2(root, 0);
        return builder.toString();
    }

    private StringBuilder renderGroup2(ConditionGroup group, int depth) {
        StringBuilder sb = new StringBuilder();
        List<ConditionNode> children = group.children();
        if (depth > 0) sb.append("(");
        for(ConditionNode child : children) {
            if (child instanceof ConditionGroup g) {
                StringBuilder s = renderGroup2(g, depth + 1);

                String s1 = s.substring(s.length() - 1);
                sb.append(s);
            } else if (child instanceof ConditionLeaf l) {
                ConditionExpression expr = l.condition();
                if (expr instanceof ComparisonBinaryCondition binary) {
                    sb.append(binary.left());
                    sb.append(binary.operator().symbol());
                    sb.append(binary.right());
                    sb.append(' '+binary.logicalOperator().name());
                }
            }
        }
        if (depth > 0) sb.append(")");
        return sb;
    }

    private String renderGroup(ConditionGroup group) {
        List<ConditionNode> children = group.children();
        List<String> rendered = new ArrayList<>();

        for (int i = 0; i < children.size(); i++) {
            ConditionNode node = children.get(i);
            String expr = renderNode(node);
            if (!expr.isBlank()) {
                rendered.add(expr);

                // 논리 연산자는 현재 노드의 logicalOperator 기준으로 다음 노드와 연결
                if (i < children.size() - 1 && node instanceof ConditionLeaf leaf) {
                    rendered.add(((ComparisonBinaryCondition)leaf.condition()).logicalOperator().name());
                }
            }
        }

        // 전체 그룹은 항상 괄호로 감싸기
        return "(" + String.join(" ", rendered) + ")";
    }

    private String renderNode(ConditionNode node) {
        if (node instanceof ConditionGroup g) return renderGroup(g);
        if (node instanceof ConditionLeaf l) return renderLeaf(l);
        return "";
    }

    private String renderLeaf(ConditionLeaf leaf) {
        ConditionExpression expr = leaf.condition();
        if (expr instanceof BinaryCondition binary) {
//            return renderBinaryCondition(binary);
            return binary.left() + " " + binary.operator().symbol() + " " + binary.right();
        }
        return "";
    }

    /*private String renderBinaryCondition(BinaryCondition cond) {
        String left = renderOperand(cond.field());
        String right = renderOperand(cond.value());
        String operator = cond.operator().symbol();
        return String.format("%s %s %s", left, operator, right);
    }

    private String renderOperand(Object operand) {
        if (operand instanceof EntityExpression expr) {
            String methodName = expr.getMethod().getName();
            String field = methodName.startsWith("get")
                    ? Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4)
                    : methodName;
            return expr.getEntity().alias() + "." + field;
        } else if (operand instanceof ObjectCapturedVariable captured) {
            Object value = captured.value();
            if (value instanceof String) return "'" + value + "'";
            return String.valueOf(value);
        } else if (operand instanceof EntityVariable var) {
            return var.alias();
        }
        return String.valueOf(operand);
    }*/
}
