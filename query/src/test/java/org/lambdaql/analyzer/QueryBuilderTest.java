package org.lambdaql.analyzer;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.jinq.jpa.JinqJPAStreamProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lambdaql.function.JpqlFunction;
import org.lambdaql.query.*;
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
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {QueryBuilderTest.TestContext.class})
@TestPropertySource(properties = "spring.jta.enabled=false")
@ActiveProfiles("test")
@Slf4j
class QueryBuilderTest {

    @Autowired
    DataSource dataSource;
    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    private int nonStaticLambda = 100;

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
        SelectQuery<Order> query = queryBuilder.from(Order.class);

        query.orderBy(o-> {
//            return List.of(o::getCustomer, o::getCustomer);
            List<Direction> asc = List.of(Direction.asc(o::getCustomer));
            return asc;
        });
        SelectQuery.SelectWhere<Order> where0 = query.where(o -> o.getId() == 10);
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
        SelectQuery<Order> query = queryBuilder.from(Order.class);
//        query.where(o->
//            o.getId() == param1
//        );
//        query.where(o->
//                o.getId() == param1
//        );
//        query.where(o->
//                o.getId() == param2
//        );
//        query.where(o->
//                o.getId() == param3
//        );
//        query.where(o->
//                o.getId() == param4
//        );
//        query.where(o->
//                o.getDescription().equals(param5)
//        );
//        query.where(o->
//                o.isActive() == param6
//        );
//        query.where(o->
//                o.getId() == param7
//        );
//        query.where(o->
//                o.getId() == param8
//        );
        query.where(o ->
                o.getImage().length == param9.length
        );

        query.where(o ->
                o.getId() == param11
        );

        query.where(o ->
                o.getId() == param12
        );

        System.out.println("test");
    }

    @Test
    void selectOrLongConditions() {
        long p1 = 70;
        long p2 = 10;
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);

        SelectQuery.SelectWhere<Order> where = query.where(o ->
            p1 == 1 + p2 || p1 != 100 || p1 > 2 || p1 >=2 || p1 < 100 || p1<=100
        );
    }

    @Test
    void selectAndLongConditions() {
        long p1 = 70;
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);

        SelectQuery.SelectWhere<Order> where = query.where(o ->
                p1 == 1 && p1 != 100 && p1 > 1 && p1 >=1 && p1 < 100 && p1<=100
        );
    }

    @Test
    void selectUnBracketsConditions() {
        boolean p0 = true;
        boolean p1 = false;
        boolean p2 = false;
        boolean p3 = true;
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);

        SelectQuery.SelectWhere<Order> where = query.where(o ->
                p0 && p1 || p0 || p2 && p3
        );
    }

    @Test
    void selectBracketsConditions() {
        boolean p0 = true;
        boolean p1 = false;
        boolean p2 = false;
        boolean p3 = true;
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);

        SelectQuery.SelectWhere<Order> where = query.where(o ->
                p0 && (p1 || p0 || p2) && p3
        );
    }

    @Test
    void selectBrackets2Conditions() {
        /**
         * 0 = {ComparisonBinaryCondition@10906} "ComparisonBinaryCondition(super=BinaryCondition(left=ObjectCapturedVariable[type=boolean, typeSignature=boolean, value=true, sequenceIndex=0, opcodeIndex=0], right=1, operator=NE(<>), originOperator=NE(<>)), labelInfo=LabelInfo(labelInfo=L448432504, value=null))"
         * 1 = {ComparisonBinaryCondition@10907} "ComparisonBinaryCondition(super=BinaryCondition(left=ObjectCapturedVariable[type=boolean, typeSignature=boolean, value=false, sequenceIndex=1, opcodeIndex=1], right=1, operator=NE(<>), originOperator=NE(<>)), labelInfo=LabelInfo(labelInfo=L448432504, value=null))"
         * 2 = {ComparisonBinaryCondition@10908} "ComparisonBinaryCondition(super=BinaryCondition(left=ObjectCapturedVariable[type=boolean, typeSignature=boolean, value=false, sequenceIndex=2, opcodeIndex=2], right=1, operator=EQ(=), originOperator=EQ(=)), labelInfo=LabelInfo(labelInfo=L1625676573, value=true))"
         * 3 = {ComparisonBinaryCondition@10909} "ComparisonBinaryCondition(super=BinaryCondition(left=ObjectCapturedVariable[type=boolean, typeSignature=boolean, value=false, sequenceIndex=1, opcodeIndex=1], right=1, operator=NE(<>), originOperator=NE(<>)), labelInfo=LabelInfo(labelInfo=L524225829, value=null))"
         * 4 = {ComparisonBinaryCondition@10910} "ComparisonBinaryCondition(super=BinaryCondition(left=ObjectCapturedVariable[type=boolean, typeSignature=boolean, value=false, sequenceIndex=2, opcodeIndex=2], right=1, operator=EQ(=), originOperator=EQ(=)), labelInfo=LabelInfo(labelInfo=L1625676573, value=true))"
         * 5 = {ComparisonBinaryCondition@10911} "ComparisonBinaryCondition(super=BinaryCondition(left=ObjectCapturedVariable[type=boolean, typeSignature=boolean, value=false, sequenceIndex=2, opcodeIndex=2], right=1, operator=EQ(=), originOperator=NE(<>)), labelInfo=LabelInfo(labelInfo=L854246299, value=false))"
         * 6 = {ComparisonBinaryCondition@10912} "ComparisonBinaryCondition(super=BinaryCondition(left=ObjectCapturedVariable[type=boolean, typeSignature=boolean, value=true, sequenceIndex=3, opcodeIndex=3], right=1, operator=EQ(=), originOperator=NE(<>)), labelInfo=LabelInfo(labelInfo=L854246299, value=false))"
         */
        boolean p0 = true;
        boolean p1 = false;
        boolean p2 = false;
        boolean p3 = true;
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);

        SelectQuery.SelectWhere<Order> where = query.where(o ->
                ((p0 == true && p1 == true && p2 == true) || (p1 == true && p2 == true)) || (p2 == true && p3 == true)
        );
    }

    @Test
    void selectBrackets3Conditions() {

        boolean p0 = true;
        boolean p1 = true;
        boolean p2 = false;
        boolean p3 = true;
        boolean p4 = false;
        boolean p5 = false;
        boolean p6 = true;
        boolean p7 = false;

        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);

        /**
         0 = {ComparisonBinaryCondition@14165} "ComparisonBinaryCondition(labelInfo=LabelInfo(labelInfo=L639313883, value=null))"
         1 = {ComparisonBinaryCondition@14166} "ComparisonBinaryCondition(labelInfo=LabelInfo(labelInfo=L639313883, value=null))"
         2 = {ComparisonBinaryCondition@14167} "ComparisonBinaryCondition(labelInfo=LabelInfo(labelInfo=L1739770043, value=null))"
         3 = {ComparisonBinaryCondition@14168} "ComparisonBinaryCondition(labelInfo=LabelInfo(labelInfo=L1739770043, value=null))"
         4 = {ComparisonBinaryCondition@14169} "ComparisonBinaryCondition(labelInfo=LabelInfo(labelInfo=L52439501, value=false))"
         5 = {ComparisonBinaryCondition@14170} "ComparisonBinaryCondition(labelInfo=LabelInfo(labelInfo=L52439501, value=false))"
         6 = {ComparisonBinaryCondition@14171} "ComparisonBinaryCondition(labelInfo=LabelInfo(labelInfo=L52439501, value=false))"
         7 = {ComparisonBinaryCondition@14172} "ComparisonBinaryCondition(labelInfo=LabelInfo(labelInfo=L52439501, value=false))"
         */
        SelectQuery.SelectWhere<Order> where1 = query.where(o ->
                (p0 == true && p1 == true && p2 == true || (p3 == true || p4 == true && p7 == true)) && p5 == true && p6 == true
        );

        /**
         * 0 = {ComparisonBinaryCondition@14194} "ComparisonBinaryCondition(labelInfo=LabelInfo(labelInfo=L1874140695, value=null))"
         * 1 = {ComparisonBinaryCondition@14195} "ComparisonBinaryCondition(labelInfo=LabelInfo(labelInfo=L1874140695, value=null))"
         * 2 = {ComparisonBinaryCondition@14196} "ComparisonBinaryCondition(labelInfo=LabelInfo(labelInfo=L1915567579, value=true))"
         * 3 = {ComparisonBinaryCondition@14197} "ComparisonBinaryCondition(labelInfo=LabelInfo(labelInfo=L1914683944, value=null))"
         * 4 = {ComparisonBinaryCondition@14198} "ComparisonBinaryCondition(labelInfo=LabelInfo(labelInfo=L632847899, value=false))"
         * 5 = {ComparisonBinaryCondition@14199} "ComparisonBinaryCondition(labelInfo=LabelInfo(labelInfo=L632847899, value=false))"
         * 6 = {ComparisonBinaryCondition@14200} "ComparisonBinaryCondition(labelInfo=LabelInfo(labelInfo=L632847899, value=false))"
         * 7 = {ComparisonBinaryCondition@14201} "ComparisonBinaryCondition(labelInfo=LabelInfo(labelInfo=L632847899, value=false))"
         */
        SelectQuery.SelectWhere<Order> where2 = query.where(o ->
                p0 == true && p1 == true && p2 == true || (p3 == true || p4 == true && p7 == true) && p5 == true && p6 == true
        );
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
        long[] param9 = {1, 2, 3};
        Object param10 = new Object();
        Object param11 = 200L;
        Long param12 = null;
        int param13 = 300;

        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);

        SelectQuery.SelectWhere<Order> where1 = query.where(o -> o.getId() == nonStaticLambda
                || o.getId() == param1
                || o.getId() == param2
                || o.getTex() == param3
                || o.getPrice() == param4
                || o.getDescription().equals(param5)
                || o.isActive() == param6
                || o.getStatus() == param7
                || o.getId() == param8
                || o.getImage().length == param9.length
                || o.getId() == ((Long) param10)
                || o.getId() == param11
                || o.getId() == param12
                || o.getId() == (long) param13
        );
        System.out.println(where1.toString());
    }

    @Test
    void selectFromBasicAnd() {
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);
        SelectQuery.SelectWhere<Order> where0 = query.where(o -> o.getId() >= 0 && o.getId() <= 1);
        assertEquals("SELECT * FROM orders", where0.toString());
    }

    @Test
    void selectFromDate() {
        Date now = new Date();
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);
        SelectQuery.SelectWhere<Order> where0 = query.where(o -> o.getId() == 10);
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
        SelectQuery<Order> query = queryBuilder.from(Order.class);

        JpqlFunction.between(1, 2, 3);
        SelectQuery.SelectWhere<Order> where1 = query.where(o -> o.getId() == param1 || o.getId() == param2 || o.getId() == param3);
        SelectQuery.SelectWhere<Order> where2 = query.where(o -> o.getId() == param1 || (o.getId() < 100 && o.getId() >= param1));
        SelectQuery.SelectWhere<Order> where3 = query.where(o -> o.getId() == order.getId() || (o.getId() < 100 && o.getId() >= param1));


        SelectQuery.SelectWhere<Order> where4 = query.where(o -> o.getCustomer().getId() == 1);
        SelectQuery<Order> select = where1.select(o -> o.getProduct().toUpperCase());


        SelectLeftJoinQuery<Order, Customer> join = query.leftJoin(Customer.class, (o, c) -> o.getCustomer().getId() == c.getId());
        //join.select((o, c) -> c.getName());
        //assertEquals("SELECT * FROM orders", queryBuilder.getQuery());
    }

    @Test
    void selectAndStack() {
        long param1 = 70;
        short param2 = 100;
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);

        SelectQuery.SelectWhere<Order> where1 = query.where(o -> param1 >= 100 && param2 <= 200);
    }

    @Test
    void selectAndStackNotConditions() {
        boolean p10 = false; // boolean 추가 (false, 부정 조건에 사용)
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);

        SelectQuery.SelectWhere<Order> where = query.where(o ->
                        !p10
        );
    }

    @Test
    void selectAndStackTernaryConditions() {
        short p3 = 200;
        boolean enableP1 =true;
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);
        SelectQuery.SelectWhere<Order> where = query.where(o ->
                (enableP1? p3 == 250: p3 == 100)
        );
    }

    @Test
    void selectAndStack_RepresentativeConditions() {
        long p1 = 70;
        int p2 = 150;
        short p3 = 200;
        byte p4 = 50;
        double p5 = 99.9;
        float p6 = 120.5f;
        long p7 = 100;
        int p8 = 80;
        boolean p9 = true;   // boolean 추가 (true)
        boolean p10 = false; // boolean 추가 (false, 부정 조건에 사용)
        boolean enableP1 =true;

        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);

        SelectQuery.SelectWhere<Order> where = query.where(o ->
                (
                        (p1 == 1 || p1 != 100 || p1 > 1 || p1 >=1 || p1 < 100 || p1<=100)         // >, <
                ) &&
                (
                    (p1 == 70 && p2 != 100) ||    // ==, !=
                                (p3 > 150 && p4 < 60)         // >, <
                ) &&
                (
                    (p5 <= 100.0 || p6 >= 120.0f) &&  // <=, >=
                                        (p7 >= 90 && (p8 < 100 || p2 > 140)) // >=, <, >  (괄호 우선순위)
                ) &&
                !(p3 == 250) &&
                        p9 &&    // ✅ boolean true 조건
                        !p10     // ✅ boolean false 부정 조건
                && (enableP1? p3 == 250: p3 == 100)
        );
    }

    @Test
    void selectAndStack_RepresentativeConditions2() {
        List<ObjectCapturedVariable> capturedVars = List.of(
                new ObjectCapturedVariable(Long.class, 70L, 0, 0),
                new ObjectCapturedVariable(Integer.class, 150, 1, 2),
                new ObjectCapturedVariable(Short.class, (short) 200, 2, 3),
                new ObjectCapturedVariable(Byte.class, (byte) 50, 3, 4),
                new ObjectCapturedVariable(Double.class, 99.9, 4, 5),
                new ObjectCapturedVariable(Float.class, 120.5f, 5, 7),
                new ObjectCapturedVariable(Long.class, 100L, 6, 8),
                new ObjectCapturedVariable(Integer.class, 80, 7, 10),
                new ObjectCapturedVariable(Boolean.class, true, 8, 11),
                new ObjectCapturedVariable(Boolean.class, false, 9, 12),
                new ObjectCapturedVariable(Boolean.class, true, 10, 13),
                new ObjectCapturedVariable(Order.class, null, 11, 14)
        );

        LambdaOpcodeAnalyzer analyzer = new LambdaOpcodeAnalyzer(capturedVars);

        // (p1 == 70)
        analyzer.visitVarInsn(0);
        analyzer.visitLdcInsn(70L);
        analyzer.visitComparison(BinaryOperator.EQ);

        // (p2 != 100)
        analyzer.visitVarInsn(2);
        analyzer.visitIntInsn(100);
        analyzer.visitComparison(BinaryOperator.NE);
        analyzer.visitLogicalOp(LogicalOperator.AND, 2);

        // (p3 > 150)
        analyzer.visitVarInsn(3);
        analyzer.visitIntInsn(150);
        analyzer.visitComparison(BinaryOperator.GT);

        // (p4 < 60)
        analyzer.visitVarInsn(4);
        analyzer.visitIntInsn(60);
        analyzer.visitComparison(BinaryOperator.LT);
        analyzer.visitLogicalOp(LogicalOperator.AND, 2);

        // OR 두개
        analyzer.visitLogicalOp(LogicalOperator.OR, 2);

        // (p5 <= 100.0)
        analyzer.visitVarInsn(5);
        analyzer.visitLdcInsn(100.0);
        analyzer.visitComparison(BinaryOperator.LE);

        // (p6 >= 120.0)
        analyzer.visitVarInsn(7);
        analyzer.visitLdcInsn(120.0f);
        analyzer.visitComparison(BinaryOperator.GE);
        analyzer.visitLogicalOp(LogicalOperator.OR, 2);

        // (p7 >= 90)
        analyzer.visitVarInsn(8);
        analyzer.visitIntInsn(90);
        analyzer.visitComparison(BinaryOperator.GE);

        // (p8 < 100)
        analyzer.visitVarInsn(10);
        analyzer.visitIntInsn(100);
        analyzer.visitComparison(BinaryOperator.LT);

        // (p2 > 140)
        analyzer.visitVarInsn(2);
        analyzer.visitIntInsn(140);
        analyzer.visitComparison(BinaryOperator.GT);
        analyzer.visitLogicalOp(LogicalOperator.OR, 2);
        analyzer.visitLogicalOp(LogicalOperator.AND, 2);

        // (p5 <= 100 OR p6 >= 120) AND (p7 >= 90 AND (p8 < 100 OR p2 > 140))
        analyzer.visitLogicalOp(LogicalOperator.AND, 2);

        // NOT(p3 == 250)
        analyzer.visitVarInsn(3);
        analyzer.visitIntInsn(250);
        analyzer.visitComparison(BinaryOperator.EQ);
        analyzer.visitLogicalOp(LogicalOperator.NOT, 1);

        // (p9 == true)
        analyzer.visitVarInsn(11);
        analyzer.visitLdcInsn(true);
        analyzer.visitComparison(BinaryOperator.EQ);

        // !(p10 == true)
        analyzer.visitVarInsn(12);
        analyzer.visitLdcInsn(true);
        analyzer.visitComparison(BinaryOperator.EQ);
        analyzer.visitLogicalOp(LogicalOperator.NOT, 1);

        // 삼항연산자 (enableP1 ? (p3 == 250) : (p3 == 100))
        analyzer.visitVarInsn(13); // enableP1
        analyzer.visitLogicalOp(LogicalOperator.NOT, 1);

        // (p3 == 250)
        analyzer.visitVarInsn(3);
        analyzer.visitIntInsn(250);
        analyzer.visitComparison(BinaryOperator.EQ);

        // (p3 == 100)
        analyzer.visitVarInsn(3);
        analyzer.visitIntInsn(100);
        analyzer.visitComparison(BinaryOperator.EQ);

        // (enableP1 ? (p3 == 250) : (p3 == 100)) 이 TernaryCondition으로 조립됨

        // 전체 AND
        analyzer.visitLogicalOp(LogicalOperator.AND, 6);

        // 결과 복원
        ConditionExpression result = analyzer.build();
        String rendered = ConditionExpressionRenderer.render(result);

        System.out.println("\n[최종 복원된 조건식]");
        System.out.println(rendered);

        assertNotNull(result);
        assertTrue(rendered.contains("?")); // 3항 연산자가 정상 복원됐는지 확인
    }

    @Test
    void selectOrStack() {
        long param1 = 70;
        short param2 = 100;
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);

        SelectQuery.SelectWhere<Order> where1 = query.where(o -> param1 >= 100 || param2 <= 200);
    }

    @Test
    void selectWhereStack() {
        long param1 = 70;
        short param2 = 100;
        Order order = new Order();
        order.setId(10L);
        order.setDescription(" test ");
        LocalDate localDate = LocalDate.now();
        LocalTime localTime = LocalTime.now();
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);

        SelectQuery.SelectWhere<Order> where1 = query.where(o -> o.items(2, ((int) param2) + (100 * 2)) && o.getId() == param1 && o.getDescription() == order.getDescription().trim() && o.getUpdateAt() == LocalDateTime.of(localDate, localTime.plusHours(10)));
    }

    @Test
    void selectNewObjectStack() {
        long param1 = 70;
        int param2 = 100;
        Order order = new Order();
        order.setId(10L);
        order.setDescription(" test ");
        LocalDate localDate = LocalDate.now();
        LocalTime localTime = LocalTime.now();
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);

        SelectQuery.SelectWhere<Order> where1 = query.where(o -> new Order().isActive());
    }

    @Test
    void selectWhereMethodStack() {
        long param1 = 70;
        Order order = new Order();
        order.setId(10L);
        order.setDescription(" test ");
        LocalDate localDate = LocalDate.now();
        LocalTime localTime = LocalTime.now();
        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);
        SelectQuery.SelectWhere<Order> where1 = query.where(o -> o.getDescription().trim() == order.getDescription().trim());
//        SelectQuery<Order> where2 = query.where(o -> LocalDateTime.of(localDate, localTime.plusHours(10)) == o.getUpdateAt());
//        SelectQuery<Order> where1 = query.where(o -> o.getId() == param1 && o.getDescription() == order.getDescription().trim() && o.getUpdateAt() == LocalDateTime.of(localDate, localTime.plusHours(10)));
        System.out.println(where1);
    }

    boolean bool() {
        return false;
    }

    @Test
    void selectWhereThisMethodStack() {

        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);
        SelectQuery.SelectWhere<Order> where1 = query.where(obj -> this.bool());
        query.where(JpqlFunction::isNull);
//
        System.out.println(where1);
    }

    @Test
    void selectWhereStaticMethodStack() {

        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);
        SelectQuery.SelectWhere<Order> where1 = query.where(obj -> JpqlFunction.like("aaa", "aa%"));
        //query.where(JpqlFunction::isNull);
//
        System.out.println(where1);
    }

    @Test
    void selectNewWhereStack() {
        long param1 = 70;

        QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
        SelectQuery<Order> query = queryBuilder.from(Order.class);
        SelectQuery.SelectWhere<Order> where1 = query.where(new SelectQuery.Where<Order>() {
            @Override
            public boolean clause(Order obj) {
                return JpqlFunction.isNull(obj);
            }
        });
//
        System.out.println(where1);
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
                .where(c -> c.getName().equals("Bob") && c.getId() == customer.getId())
                .toList();
        System.out.println(customers);
    }

    @Test
    void delete() {
    }

    @Test
    void insert() {
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableJpaRepositories(basePackages = {"org.lambdaql"}) // JPA 스캔
    @EntityScan("org.lambdaql")
    public static class TestContext {
    }
}