package org.lambdaql.analyzer;

import lombok.Getter;

import java.lang.reflect.Method;

@Getter
public class EntityExpression {
    private final LambdaEntityValue entity;
    private final Method method;

    public EntityExpression(LambdaEntityValue entity, Method method) {
        this.entity = entity;
        this.method = method;
    }
}
