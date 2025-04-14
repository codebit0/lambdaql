package org.lambdaql.query;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import lombok.Getter;

public class EntityColumnResolver {
    private final Metamodel metamodel;
    @Getter
    private final EntityManagerFactory entityManagerFactory;

    public EntityColumnResolver(Metamodel metamodel, EntityManagerFactory entityManagerFactory) {
        this.metamodel = metamodel;
        this.entityManagerFactory = entityManagerFactory;
    }

    public String resolveColumnName(Class<?> entityClass, String fieldName) {
        try {
            EntityType<?> entityType = metamodel.entity(entityClass);
            Attribute<?, ?> attr = entityType.getAttribute(fieldName);

            if (attr instanceof org.hibernate.metamodel.model.domain.internal.SingularAttributeImpl) {
//                var hAttr = (org.hibernate.metamodel.model.domain.internal.SingularAttributeImpl<?, ?>) attr;
//                return hAttr.getColumnMappings().get(0).getColumnName().getText();
            }
        } catch (Exception e) {
            // fallback
        }

        return fieldName;
    }
}
