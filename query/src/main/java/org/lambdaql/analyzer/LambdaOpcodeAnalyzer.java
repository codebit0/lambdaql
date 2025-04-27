package org.lambdaql.analyzer;

import jdk.jfr.Threshold;
import lombok.ToString;
import org.objectweb.asm.Label;
import org.objectweb.asm.util.Printer;

import java.util.*;

public class LambdaOpcodeAnalyzer {

    private final Map<Integer, ObjectCapturedVariable> capturedVariables = new HashMap<>();
    private final Deque<Object> valueStack = new ArrayDeque<>();
    private final List<Instruction> instructions = new ArrayList<>();
    private int pc = 0;

    public LambdaOpcodeAnalyzer(List<ObjectCapturedVariable> capturedVars) {
        for (ObjectCapturedVariable v : capturedVars) {
            capturedVariables.put(v.opcodeIndex(), v);
        }
    }

    public void visitVarInsn(int varIndex) {
        instructions.add(new Instruction("visitVarInsn", varIndex, null));
    }

    public void visitLdcInsn(Object constant) {
        instructions.add(new Instruction("visitLdcInsn", -1, constant));
    }

    public void visitIntInsn(Object constant) {
        instructions.add(new Instruction("visitIntInsn", -1, constant));
    }

    public void visitComparison(BinaryOperator operator) {
        instructions.add(new Instruction("visitComparison", -1, operator));
    }

    public void visitLogicalOp(LogicalOperator logicalOp, int exprCount) {
        instructions.add(new Instruction("visitLogicalOp", exprCount, logicalOp));
    }

    public void visitLabel(Label label) {
        instructions.add(new Instruction("visitLabel", -1, label));
    }

    public void visitJumpInsn(int opcode, Label label) {
        instructions.add(new Instruction("visitJumpInsn", opcode, new JumpInfo(Printer.OPCODES[opcode], label)));
    }

    public ConditionExpression build() {
        pc = 0;
        return parseExpression();
    }

    private ConditionExpression parseExpression() {
        Deque<ConditionExpression> stack = new ArrayDeque<>();
        while (pc < instructions.size()) {
            Instruction inst = instructions.get(pc);
            System.out.println("Processing instruction: " + inst);
            switch (inst.type) {
                case "visitVarInsn" -> valueStack.push(capturedVariables.get(inst.varIndex));
                case "visitLdcInsn", "visitIntInsn" -> valueStack.push(inst.operand);
                case "visitComparison" -> {
                    Object right = popValue();
                    Object left = popValue();
                    BinaryOperator operator = (BinaryOperator) inst.operand;
                    stack.push(BinaryCondition.of(resolveOperand(left), operator, resolveOperand(right)));
                }
                case "visitLogicalOp" -> {
                    LogicalOperator op = (LogicalOperator) inst.operand;
                    int count = inst.varIndex;
                    if (op == LogicalOperator.NOT) {
                        ConditionExpression inner = stack.pop();
                        stack.push(new NotCondition(inner));
                    } else {
                        List<ConditionExpression> conditions = new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            conditions.add(stack.pop());
                        }
                        Collections.reverse(conditions);
                        stack.push(new LogicalCondition(op, conditions));
                    }
                }
                case "visitJumpInsn" -> {
                    JumpInfo jump = (JumpInfo) inst.operand;
                    if (jump.opcode.equals("IFEQ") || jump.opcode.equals("IFNE")) {
                        ConditionExpression condition = stack.pop();

                        int trueStart = pc + 1;
                        int falseStart = labelPositions(jump.label);

                        pc = trueStart;
                        ConditionExpression trueBranch = parseBranchUntilGoto();

                        pc++; // skip GOTO
                        pc = falseStart;
                        ConditionExpression falseBranch = parseBranchUntilLabel();

                        stack.push(new TernaryCondition(condition, trueBranch, falseBranch));
                        continue;
                    }
                }
                case "visitLabel" -> {}
            }
            pc++;
        }
        return stack.isEmpty() ? null : stack.pop();
    }

    private ConditionExpression parseBranchUntilGoto() {
        Deque<ConditionExpression> stack = new ArrayDeque<>();
        while (pc < instructions.size()) {
            Instruction inst = instructions.get(pc);
            if ("visitJumpInsn".equals(inst.type) && "GOTO".equals(((JumpInfo) inst.operand).opcode)) {
                break;
            }
            switch (inst.type) {
                case "visitVarInsn" -> valueStack.push(capturedVariables.get(inst.varIndex));
                case "visitLdcInsn", "visitIntInsn" -> valueStack.push(inst.operand);
                case "visitComparison" -> {
                    Object right = popValue();
                    Object left = popValue();
                    BinaryOperator operator = (BinaryOperator) inst.operand;
                    stack.push(BinaryCondition.of(resolveOperand(left), operator, resolveOperand(right)));
                }
                case "visitLogicalOp" -> {
                    LogicalOperator op = (LogicalOperator) inst.operand;
                    int count = inst.varIndex;
                    if (op == LogicalOperator.NOT) {
                        ConditionExpression inner = stack.pop();
                        stack.push(new NotCondition(inner));
                    } else {
                        List<ConditionExpression> conditions = new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            conditions.add(stack.pop());
                        }
                        Collections.reverse(conditions);
                        stack.push(new LogicalCondition(op, conditions));
                    }
                }
            }
            pc++;
        }
        return stack.isEmpty() ? null : stack.pop();
    }

    private ConditionExpression parseBranchUntilLabel() {
        Deque<ConditionExpression> stack = new ArrayDeque<>();
        while (pc < instructions.size()) {
            Instruction inst = instructions.get(pc);
            if ("visitLabel".equals(inst.type)) {
                break;
            }
            switch (inst.type) {
                case "visitVarInsn" -> valueStack.push(capturedVariables.get(inst.varIndex));
                case "visitLdcInsn", "visitIntInsn" -> valueStack.push(inst.operand);
                case "visitComparison" -> {
                    Object right = popValue();
                    Object left = popValue();
                    BinaryOperator operator = (BinaryOperator) inst.operand;
                    stack.push(BinaryCondition.of(resolveOperand(left), operator, resolveOperand(right)));
                }
                case "visitLogicalOp" -> {
                    LogicalOperator op = (LogicalOperator) inst.operand;
                    int count = inst.varIndex;
                    if (op == LogicalOperator.NOT) {
                        ConditionExpression inner = stack.pop();
                        stack.push(new NotCondition(inner));
                    } else {
                        List<ConditionExpression> conditions = new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            conditions.add(stack.pop());
                        }
                        Collections.reverse(conditions);
                        stack.push(new LogicalCondition(op, conditions));
                    }
                }
            }
            pc++;
        }
        return stack.isEmpty() ? null : stack.pop();
    }

    private int labelPositions(Label label) {
        for (int i = 0; i < instructions.size(); i++) {
            Instruction inst = instructions.get(i);
            if ("visitLabel".equals(inst.type) && inst.operand == label) {
                return i + 1; // ❗ 수정: 라벨 다음부터 시작
            }
        }
        throw new IllegalStateException("Label not found: " + label);
    }

    private Object popValue() {
        if (valueStack.isEmpty()) throw new IllegalStateException("Value stack is empty");
        return valueStack.pop();
    }

    private Object resolveOperand(Object operand) {
        if (operand instanceof ObjectCapturedVariable var) {
            return var;
        }
        return operand;
    }

    @ToString
    private static class Instruction {
        final String type;
        final int varIndex;
        final Object operand;

        Instruction(String type, int varIndex, Object operand) {
            this.type = type;
            this.varIndex = varIndex;
            this.operand = operand;
        }
    }

    private static class JumpInfo {
        final String opcode;
        final Label label;

        JumpInfo(String opcode, Label label) {
            this.opcode = opcode;
            this.label = label;
        }
    }
}
