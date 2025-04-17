package org.lambdaql.query.lambda;

import java.lang.reflect.Method;

public record ColumnMethod(Method method) implements IColumn {
}
