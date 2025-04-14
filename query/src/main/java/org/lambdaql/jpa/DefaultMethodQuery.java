package org.lambdaql.jpa;

import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;

public final class DefaultMethodQuery implements RepositoryQuery {
    private final Method method;
    private final QueryMethod queryMethod;

    public DefaultMethodQuery(Method method, QueryMethod queryMethod) {
        super();
        this.method = method;
        this.queryMethod = queryMethod;
    }

    public Object execute(Object[] parameters) {
        try {
            Class<?> declaringClass = this.method.getDeclaringClass();
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup());
            MethodHandle specialMethod = lookup.findSpecial(declaringClass, this.method.getName(), MethodType.methodType(this.method.getReturnType(), this.method.getParameterTypes()), declaringClass);
            MethodHandle methodHandle = specialMethod.bindTo(parameters[0]);
            return methodHandle.invokeWithArguments(Arrays.copyOf(parameters, parameters.length));
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke default method " + this.method.getName(), e);
        }
    }

    public QueryMethod getQueryMethod() {
        return this.queryMethod;
    }
}

