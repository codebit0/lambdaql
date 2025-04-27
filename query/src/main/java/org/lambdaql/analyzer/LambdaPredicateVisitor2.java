package org.lambdaql.analyzer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.Printer;

import java.util.List;

/**
 * Lambda 내부의 Predicate를 분석하는 Visitor
 */
public class LambdaPredicateVisitor2 extends MethodVisitor {

    private final LambdaOpcodeAnalyzer analyzer;

    public LambdaPredicateVisitor2(MethodVisitor mv, List<ObjectCapturedVariable> capturedVariables) {
        super(Opcodes.ASM9, mv);
        this.analyzer = new LambdaOpcodeAnalyzer(capturedVariables);
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        super.visitVarInsn(opcode, varIndex);
        analyzer.visitVarInsn(varIndex);
    }

    @Override
    public void visitLdcInsn(Object value) {
        super.visitLdcInsn(value);
        analyzer.visitLdcInsn(value);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand);
        analyzer.visitIntInsn(operand);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
        analyzer.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
        analyzer.visitLabel(label);
    }

    /**
     * 논리 연산자를 수동으로 삽입할 때 사용
     * ex) LogicalOperator.NOT, LogicalOperator.AND, LogicalOperator.OR
     */
    public void visitLogicalOp(LogicalOperator operator, int count) {
        analyzer.visitLogicalOp(operator, count);
    }

    public ConditionExpression getConditionExpression() {
        return analyzer.build();
    }
}
