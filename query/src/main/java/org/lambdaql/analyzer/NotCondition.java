package org.lambdaql.analyzer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * NOT (Condition) 표현
 */
@Getter
@RequiredArgsConstructor
public class NotCondition extends ConditionExpression {
    private final ConditionExpression inner;
}
