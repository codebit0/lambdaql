package org.lambdaql.query;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.jinq.jpa.JinqJPAStreamProvider;
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
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest( classes = { QueryBuilderTest.TestContext.class })
@TestPropertySource(properties = "spring.jta.enabled=false")
@ActiveProfiles("test")
@Slf4j
class QueryBuilderTest {

    @Configuration
    @EnableAutoConfiguration
    @EnableJpaRepositories(basePackages = {"org.lambdaql"}) // JPA 스캔
    @EntityScan("org.lambdaql")
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
    void selectFromBasic() {
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.selectFrom(Order.class);
        SelectQuery<Order> where0 = query.where(o -> o.getId() == 10);
        assertEquals("SELECT * FROM orders", where0.toString());
    }

    @Test
    void selectFromBasicAnd() {
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.selectFrom(Order.class);
        SelectQuery<Order> where0 = query.where(o -> o.getId() >= 0 && o.getId() <= 1);
        assertEquals("SELECT * FROM orders", where0.toString());
    }

    @Test
    void selectFromDate() {
        Date now = new Date();
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.selectFrom(Order.class);
        SelectQuery<Order> where0 = query.where(o -> o.getId() == 10);
        //assertEquals("SELECT * FROM orders", queryBuilder.getQuery());
    }

    @Test
    void selectFrom2() {
        long param1 = 70;
        long param2 = 90;
        long param3 = 120;
        Order order = new Order();
        order.setId(10);
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.selectFrom(Order.class);

        SelectQuery<Order> where1 = query.where(o -> o.getId() == param1 || o.getId() == param2 || o.getId() == param3);
        SelectQuery<Order> where2 = query.where(o -> o.getId() == param1 || (o.getId() < 100 && o.getId() >= param1));
        SelectQuery<Order> where3 = query.where(o -> o.getId() == order.getId() || (o.getId() < 100 && o.getId() >= param1));


        SelectQuery<Order> where4 = query.where(o -> o.getCustomer().getId() == 1);
        SelectQuery<Order> select = where1.select(o -> o.getProduct().toUpperCase());


        SelectJoinQuery<Order, Customer> join = query.leftJoin(Customer.class, (o, c) -> o.getCustomer().getId() == c.getId());
        join.select((o, c)-> c.getName());

        //assertEquals("SELECT * FROM orders", queryBuilder.getQuery());
    }

    @Test
    void update() {
        int param1 = 70;
        Order order = new Order();
        order.setId(10);
        Customer customer = new Customer(1, "Bob", List.of(order));
        JinqJPAStreamProvider streams =
                new JinqJPAStreamProvider(entityManagerFactory);
        List<Customer> customers = streams
                .streamAll(entityManagerFactory.createEntityManager(), Customer.class)
                .where( c -> c.getName().equals("Bob") && c.getId() == customer.getId() )
                .toList();
        System.out.println(customers);
    }

    @Test
    void delete() {
    }

    @Test
    void insert() {
    }
}