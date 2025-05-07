package org.lambdaql.analyzer;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TernaryCondition implements ConditionExpression {
    private final ConditionExpression condition;   // 조건 (ex: enableP1)
    private final ConditionExpression ifTrue;       // 참일 때 (ex: p3 == 250)
    private final ConditionExpression ifFalse;      // 거짓일 때 (ex: p3 == 100)
}
