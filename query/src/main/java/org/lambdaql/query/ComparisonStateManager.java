package org.lambdaql.query;

import org.objectweb.asm.Label;

import static org.lambdaql.query.LambdaPredicateVisitor.*;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;

public class ComparisonStateManager {
    private ComparisonResult comparison;
    private boolean expectedResult;
    private Label trueLabel;
    private Label falseLabel;
    private Label currentLabel;

    void captureComparison(Object left, Object right) {
        this.comparison = new ComparisonResult(left, right);
        System.out.println("🧮 Comparison captured: " + left + " vs " + right);
    }

    void registerBranch(int opcode, Label label) {
        if (opcode == IFEQ) {
            trueLabel = label;
        } else if (opcode == IFNE) {
            falseLabel = label;
        }
        System.out.println("🔍 Branch registered: " + opcode + " → " + label);
    }

    void setCurrentLabel(Label label) {
        this.currentLabel = label;
    }

    void setExpectedResult(boolean expected) {
        this.expectedResult = expected;
    }

    boolean isExpectedTrue() {
        return expectedResult;
    }

    boolean hasPendingComparison() {
        return comparison != null;
    }

    ComparisonResult consumeComparison() {
        ComparisonResult temp = this.comparison;
        this.comparison = null;
        return temp;
    }
}
