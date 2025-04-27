package org.lambdaql.analyzer;

import java.util.List;

public class LambdaOpcodeAnalyzerTest {

    public static void main(String[] args) {

        List<ObjectCapturedVariable> capturedVars = List.of(
                new ObjectCapturedVariable(Long.class, 70L, 0, 0),
                new ObjectCapturedVariable(Integer.class, 150, 1, 2),
                new ObjectCapturedVariable(Short.class, (short) 200, 2, 3),
                new ObjectCapturedVariable(Byte.class, (byte) 50, 3, 4),
                new ObjectCapturedVariable(Double.class, 99.9, 4, 5),
                new ObjectCapturedVariable(Float.class, 120.5f, 5, 7),
                new ObjectCapturedVariable(Long.class, 100L, 6, 8),
                new ObjectCapturedVariable(Integer.class, 80, 7, 10),
                new ObjectCapturedVariable(Boolean.class, true, 8, 11),   // p9
                new ObjectCapturedVariable(Boolean.class, false, 9, 12),  // p10
                new ObjectCapturedVariable(Order.class, null, 10, 13)     // Entity
        );

        LambdaOpcodeAnalyzer analyzer = new LambdaOpcodeAnalyzer(capturedVars);

        // (p1 == 70 && p2 != 100)
        analyzer.visitVarInsn(0);      // p1
        analyzer.visitLdcInsn(70L);
        analyzer.visitComparison(BinaryOperator.EQ);

        analyzer.visitVarInsn(2);      // p2
        analyzer.visitIntInsn(100);
        analyzer.visitComparison(BinaryOperator.NE);

        analyzer.visitLogicalOp(LogicalOperator.AND, 2);

        // (p3 > 150 && p4 < 60)
        analyzer.visitVarInsn(3);      // p3
        analyzer.visitIntInsn(150);
        analyzer.visitComparison(BinaryOperator.GT);

        analyzer.visitVarInsn(4);      // p4
        analyzer.visitIntInsn(60);
        analyzer.visitComparison(BinaryOperator.LT);

        analyzer.visitLogicalOp(LogicalOperator.AND, 2);

        // ( (p1==70 && p2!=100) || (p3>150 && p4<60) )
        analyzer.visitLogicalOp(LogicalOperator.OR, 2);

        // (p5 <= 100.0 || p6 >= 120.0)
        analyzer.visitVarInsn(5);      // p5
        analyzer.visitLdcInsn(100.0);
        analyzer.visitComparison(BinaryOperator.LE);

        analyzer.visitVarInsn(7);      // p6
        analyzer.visitLdcInsn(120.0f);
        analyzer.visitComparison(BinaryOperator.GE);

        analyzer.visitLogicalOp(LogicalOperator.OR, 2);

        // (p7 >= 90 && (p8 < 100 || p2 > 140))
        analyzer.visitVarInsn(8);      // p7
        analyzer.visitIntInsn(90);
        analyzer.visitComparison(BinaryOperator.GE);

        analyzer.visitVarInsn(10);     // p8
        analyzer.visitIntInsn(100);
        analyzer.visitComparison(BinaryOperator.LT);

        analyzer.visitVarInsn(2);      // p2
        analyzer.visitIntInsn(140);
        analyzer.visitComparison(BinaryOperator.GT);

        analyzer.visitLogicalOp(LogicalOperator.OR, 2);
        analyzer.visitLogicalOp(LogicalOperator.AND, 2);

        // ( (p5<=100 || p6>=120) && (p7>=90 && (p8<100 || p2>140)) )
        analyzer.visitLogicalOp(LogicalOperator.AND, 2);

        // (p3 == 250)
        analyzer.visitVarInsn(3);      // p3
        analyzer.visitIntInsn(250);
        analyzer.visitComparison(BinaryOperator.EQ);

        // !(p3 == 250)
        analyzer.visitLogicalOp(LogicalOperator.NOT, 1);

        // p9 == true
        analyzer.visitVarInsn(11);     // p9
        analyzer.visitLdcInsn(true);
        analyzer.visitComparison(BinaryOperator.EQ);

        // p10 == true (나중에 NOT 적용)
        analyzer.visitVarInsn(12);     // p10
        analyzer.visitLdcInsn(true);
        analyzer.visitComparison(BinaryOperator.EQ);
        analyzer.visitLogicalOp(LogicalOperator.NOT, 1);

        // 마지막 조합 (AND 4개)
        analyzer.visitLogicalOp(LogicalOperator.AND, 4);

        // 최종 트리 빌드
        ConditionExpression result = analyzer.build();
        String rendered = ConditionExpressionRenderer.render(result);

        System.out.println("\n[최종 복원된 조건식]");
        System.out.println(rendered);
    }
}
