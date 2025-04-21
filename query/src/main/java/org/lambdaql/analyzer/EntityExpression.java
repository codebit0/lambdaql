package org.lambdaql.analyzer;

import lombok.Getter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Getter
public class EntityExpression {
    private final LambdaEntityValue entity;
    private final Method method;
    private final List<Object> arguments;

    public EntityExpression(LambdaEntityValue entity, Method method, List<Object> arguments) {
        this.entity = entity;
        this.method = method;
        this.arguments = arguments;
    }

    public EntityExpression(LambdaEntityValue entity, Method method) {
        this.entity = entity;
        this.method = method;
        this.arguments = new ArrayList<>();
    }
}
