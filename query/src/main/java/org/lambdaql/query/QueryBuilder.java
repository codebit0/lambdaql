package org.lambdaql.query;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Metamodel;
import org.lambdaql.analyzer.EntityColumnResolver;

public class QueryBuilder {
    private final EntityManagerFactory emf;
    private final Metamodel metamodel;
    private final EntityColumnResolver columnResolver;

    public QueryBuilder(EntityManagerFactory emf) {
        this.emf = emf;
        EntityManager entityManager = emf.createEntityManager();

        this.metamodel = emf.getMetamodel();
        this.columnResolver = new EntityColumnResolver(metamodel, emf);
    }

    public <T> SelectQuery<T> selectFrom(Class<T> entityClass) {
        return new SelectQuery<>(entityClass, columnResolver);
    }


    public <T> UpdateQuery<T> update(Class<T> entityClass) {
        return new UpdateQuery<>(entityClass, columnResolver);
    }

    public <T> DeleteQuery<T> delete(Class<T> entityClass) {
        return new DeleteQuery<>(entityClass, columnResolver);
    }

    public <T> InsertQuery<T> insert(Class<T> entityClass) {
        return new InsertQuery<>(entityClass, columnResolver);
    }

    // insert(), delete(), select() 등도 비슷한 방식
}