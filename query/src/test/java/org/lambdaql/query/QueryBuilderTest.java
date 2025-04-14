package org.lambdaql.query;

import com.hunet.common.datasource.RoutingDataSource;
import com.hunet.common.datasource.annotation.EnableRoutingDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

@SpringBootTest( classes = { QueryBuilderTest.TestContext.class })
@TestPropertySource(properties = "spring.jta.enabled=false")
@ActiveProfiles("test")
@Slf4j
class QueryBuilderTest {

    @Configuration
    @EnableAutoConfiguration
    @EnableJpaRepositories(basePackages = {"com.hunet"}) // JPA 스캔
    @EntityScan("com.hunet")
    public static class TestContext {
    }

    @Autowired
    DataSource dataSource;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    public void setUp() {
//        DataSource dataSource = routingDataSource.resolvedDataSource("main");
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = resourceLoader.getResource("classpath:table.sql").getContentAsString(StandardCharsets.UTF_8);
            stmt.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void selectFrom() {
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.selectFrom(Order.class);
        SelectQuery<Order> where1 = query.where(o -> o.getId() >= 10 &&  o.getId() < 100);

        SelectQuery<Order> where2 = query.where(o -> o.getCustomer().getId() == 1);
        SelectQuery<Order> select = where1.select(o -> o.getProduct().toUpperCase());


        SelectJoinQuery<Order, Customer> join = query.leftJoin(Customer.class, (o, c) -> o.getCustomer().getId() == c.getId());
        join.select((o, c)-> c.getName());

        //assertEquals("SELECT * FROM orders", queryBuilder.getQuery());
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }

    @Test
    void insert() {
    }
}