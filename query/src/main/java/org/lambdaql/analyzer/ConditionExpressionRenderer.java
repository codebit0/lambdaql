package org.lambdaql.analyzer;

public class ConditionExpressionRenderer {

    public static String render(ConditionExpression expression) {
        if (expression instanceof BinaryCondition binary) {
            return "(" + renderOperand(binary.field()) + " " + binary.operator().symbol() + " " + renderOperand(binary.value()) + ")";
        } else if (expression instanceof LogicalCondition logical) {
            StringBuilder sb = new StringBuilder();
            String op = " " + logical.operator().name() + " ";
            for (int i = 0; i < logical.conditions.size(); i++) {
                if (i > 0) {
                    sb.append(op);
                }
                sb.append(render(logical.conditions.get(i)));
            }
            return "(" + sb + ")";
        } else if (expression instanceof NotCondition not) {
            return "(NOT " + render(not.getInner()) + ")";
        } else if (expression instanceof TernaryCondition ternary) {
            return "(" + render(ternary.getCondition()) + " ? " + render(ternary.getIfTrue()) + " : " + render(ternary.getIfFalse()) + ")";
        } else if (expression instanceof LiteralCondition literal) {
            return literal.isValue() ? "TRUE" : "FALSE";
        } else {
            throw new IllegalStateException("Unknown condition type: " + expression.getClass());
        }
    }

    private static String renderOperand(Object operand) {
        if (operand instanceof ObjectCapturedVariable var) {
            return "param" + var.sequenceIndex();
        }
        return operand.toString();
    }
}
