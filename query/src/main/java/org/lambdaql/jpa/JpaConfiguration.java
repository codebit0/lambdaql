package org.lambdaql.jpa;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilderCustomizer;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@Configuration
@ConditionalOnClass({ LocalContainerEntityManagerFactoryBean.class, EntityManager.class })
public class JpaConfiguration {

    @Bean
    public EntityManagerFactoryBuilderCustomizer entityManagerFactoryBuilderCustomizer() {
        return new EntityManagerFactoryBuilderCustomizer(){
            @Override
            public void customize(EntityManagerFactoryBuilder builder) {

            }
        };
    }

    @Bean
    public BeanFactoryPostProcessor repositoryFactoryBeanReplacer() {
        return (ConfigurableListableBeanFactory beanFactory) -> {
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

            // 기존 JpaRepositoryFactoryBean을 DefaultMethodSupportJpaRepositoryFactoryBean 변경
            String[] beanNames = registry.getBeanDefinitionNames();
            for (String beanName : beanNames) {
                ResolvableType resolvableType = registry.getBeanDefinition(beanName).getResolvableType();
                if(resolvableType.isAssignableFrom(JpaRepositoryFactoryBean.class)) {
                    registry.getBeanDefinition(beanName)
                            .setBeanClassName(DefaultMethodSupportJpaRepositoryFactoryBean.class.getName());
                }
            }
        };
    }
}
