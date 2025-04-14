package org.lambdaql.jpa;

import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Method;

public class DefaultMethodQueryLookupStrategy implements QueryLookupStrategy {

    private final QueryLookupStrategy delegate;

    public DefaultMethodQueryLookupStrategy(QueryLookupStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public RepositoryQuery resolveQuery(
            Method method,
            RepositoryMetadata metadata,
            ProjectionFactory factory,
            NamedQueries namedQueries
    ) {
        // default method는 QueryLookup에서 제외
        if (method.isDefault() ) {
            return new DefaultMethodQuery(
                    method,
                    new QueryMethod(method, metadata, factory)
            );
        }

        // 기존 QueryLookupStrategy 위임
        return delegate.resolveQuery(method, metadata, factory, namedQueries);
    }

    public static QueryLookupStrategy create(QueryLookupStrategy delegate) {
        return new DefaultMethodQueryLookupStrategy(delegate);
    }
}
