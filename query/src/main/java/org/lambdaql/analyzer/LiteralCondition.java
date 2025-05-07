package org.lambdaql.analyzer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LiteralCondition implements ConditionExpression {
    private final boolean value;

    public static LiteralCondition of(boolean value) {
        return new LiteralCondition(value);
    }
}
