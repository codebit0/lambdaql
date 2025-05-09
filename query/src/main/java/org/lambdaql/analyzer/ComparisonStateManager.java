package org.lambdaql.analyzer;

import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.Label;

import java.util.HashSet;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

@Slf4j
public class ComparisonStateManager {
    private Comparison comparison;
    private Label currentLabel;
    private final Set<Label> jumpTargets = new HashSet<>();
    private Integer lastJumpOpcode;
    private Boolean expectedResult;

    void captureComparison(Object left, Object right) {
        this.comparison = new Comparison(left, right);
        System.out.println(" // captureComparison: " + comparison);
    }

    void registerBranch(int opcode, Label label) {
        this.lastJumpOpcode = opcode;
        this.jumpTargets.add(label);
        System.out.println(" // registerBranch: "+ opcode+ " labelInfo " + label);
    }

    void setCurrentLabel(Label label) {
        this.currentLabel = label;
    }

    boolean isCurrentLabelInJumpTarget() {
        return currentLabel != null && jumpTargets.contains(currentLabel);
    }

    void setExpectedResult(boolean result) {
        this.expectedResult = result;
    }

    boolean isExpectedTrue() {
        return expectedResult != null && expectedResult;
    }

    boolean hasPendingComparison() {
        return comparison != null;
    }

    Comparison consumeComparison() {
        Comparison cr = this.comparison;
        this.comparison = null;
        return cr;
    }

    BinaryOperator resolveFinalOperator() {
        if (lastJumpOpcode == null || expectedResult == null)
            return BinaryOperator.EQ;
        return switch (lastJumpOpcode) {
            case IFEQ -> expectedResult ? BinaryOperator.EQ : BinaryOperator.NE;
            case IFNE -> expectedResult ? BinaryOperator.NE : BinaryOperator.EQ;
            case IFLT -> expectedResult ? BinaryOperator.LT : BinaryOperator.GE;
            case IFLE -> expectedResult ? BinaryOperator.LE : BinaryOperator.GT;
            case IFGT -> expectedResult ? BinaryOperator.GT : BinaryOperator.LE;
            case IFGE -> expectedResult ? BinaryOperator.GE : BinaryOperator.LT;
            default -> BinaryOperator.EQ;
        };
    }

    BinaryOperator resolveOperatorForOpcode(int opcode) {
        return switch (opcode) {
            case IFNE -> BinaryOperator.NE;
            case IFEQ -> BinaryOperator.EQ;
            case IFLT -> BinaryOperator.LT;
            case IFLE -> BinaryOperator.LE;
            case IFGT -> BinaryOperator.GT;
            case IFGE -> BinaryOperator.GE;
            default -> throw new UnsupportedOperationException("Unsupported opcode: " + opcode);
        };
    }
}
