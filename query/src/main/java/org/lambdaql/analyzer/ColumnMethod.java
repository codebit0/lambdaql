package org.lambdaql.analyzer;

import java.lang.reflect.Method;

public record ColumnMethod(Method method) implements IColumn {
}
