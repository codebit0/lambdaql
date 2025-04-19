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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private int nonStaticLambda = 100;

    @Test
    void selectFromBasic() {
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.selectFrom(Order.class);
        SelectQuery<Order> where0 = query.where(o -> o.getId() == 10);
        assertEquals("SELECT * FROM orders", where0.toString());
    }

    @Test
    void selectVarIndexTest1() {
        long param1 = 70;
        int param2 = 90;
        float param3 = 120;
        double param4 = 130;
        String param5 = "test";
        boolean param6 = true;
        short param7 = 10;
        byte param8 = 11;
        int[] param9 = {1, 2, 3};
        Object param10 = new Object();
        Object param11 = 200L;
        Long param12 = null;
        int param13 = 300;

        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.selectFrom(Order.class);
        query.where(o->
                        o.getId() == param1
        );
        /*query.where(o->
                o.getId() == param1
        );
        query.where(o->
                o.getId() == param2
        );
        query.where(o->
                o.getId() == param3
        );
        query.where(o->
                o.getId() == param4
        );
        query.where(o->
                o.getDescription().equals(param5)
        );
        query.where(o->
                o.isActive() == param6
        );
        query.where(o->
                o.getId() == param7
        );
        query.where(o->
                o.getId() == param8
        );*/
//        query.where(o->
//                o.getId().equals(param10)
//        );

        query.where(o->
                o.getId() == param11
        );

        query.where(o->
                o.getId() == param12
        );

        System.out.println("test");
    }

    @Test
    void selectVarIndexTest() {
        long param1 = 70;
        int param2 = 90;
        float param3 = 120;
        double param4 = 130;
        String param5 = "test";
        boolean param6 = true;
        short param7 = 10;
        byte param8 = 11;
        int[] param9 = {1, 2, 3};
        Object param10 = new Object();
        Object param11 = 200L;
        Long param12 = null;
        int param13 = 300;

        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.selectFrom(Order.class);

        SelectQuery<Order> where1 = query.where(o -> o.getId() == nonStaticLambda
                || o.getId() == param1
                || o.getId() == param2
                || o.getTex() == param3
                ||  o.getPrice() == param4
                ||  o.getDescription().equals(param5)
                ||  o.isActive() == param6
                ||  o.getStatus() == param7
                ||  o.getId() == param8
//                ||  o.getImage() == param9
                ||  o.getId() == ((Long) param10)
                ||  o.getId() == param11
                ||  o.getId() == param12
                ||  o.getId() == (long)param13
        );
        System.out.println(where1.toString());
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
        order.setId(10L);
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
    void selectWhereStack() {
        long param1 = 70;
        Order order = new Order();
        order.setId(10L);
        order.setDescription(" test ");
        LocalDate localDate = LocalDate.now();
        LocalTime localTime = LocalTime.now();
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.selectFrom(Order.class);

        SelectQuery<Order> where1 = query.where(o -> o.getId() == param1 && o.getDescription() == order.getDescription().trim() && o.getUpdateAt() == LocalDateTime.of(localDate, localTime.plusHours(10)));

    }

    @Test
    void update() {
        int param1 = 70;
        Order order = new Order();
        order.setId(10L);
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