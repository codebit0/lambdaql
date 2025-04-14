package org.lambdaql.jpa;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.ValueExpressionDelegate;

import java.util.Optional;

public class DefaultMethodSupportJpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID>
        extends JpaRepositoryFactoryBean<T, S, ID> {

    public DefaultMethodSupportJpaRepositoryFactoryBean(Class<T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected JpaRepositoryFactory createRepositoryFactory(EntityManager entityManager) {
        return new JpaRepositoryFactory(entityManager) {
            @Override
            public Optional<QueryLookupStrategy> getQueryLookupStrategy(
                    QueryLookupStrategy.Key key,
                    ValueExpressionDelegate evaluationContextProvider
            ) {
                Optional<QueryLookupStrategy> defaultStrategy = super.getQueryLookupStrategy(key, evaluationContextProvider);
                return defaultStrategy.map(DefaultMethodQueryLookupStrategy::create);
            }
        };
    }
}
