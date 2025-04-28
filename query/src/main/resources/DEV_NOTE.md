

lambdaql 은 lambad 식을 분석하여 JPQL로 변환하는 쿼리 DSL 입니다.

lambdaql은 3개의 구성 요소로 이루어져 있습니다.
1. 쿼리 빌더 DSL
2. 람다 분석 및 메서드 분석
3. 쿼리 빌더 결과를 렌더링 하는 Renderer
   1. 쿼리 빌더 분석 결과를 JPQL로 변환하는 JPQLRenderer
   2. 쿼리 빌더 분석 결과를 SQL로 변환하는 SQLRenderer
   3. 쿼리 빌더  분석 결과를 재 복원하는 디버그용 LambdaRenderer

select 예시
```java
long param1 = 70;
Order order = new Order();
order.setId(10L);
order.setDescription(" test ");
order.setActive(true);
        
QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
SelectQuery<Order> query = queryBuilder.from(Order.class);
SelectQuery.SelectWhere<Order> where1 = query.where(o -> order.isActive());
SelectQuery.SelectWhere<Order> where2 = query.where(o -> o.getDescription().trim() == order.getDescription().trim());
SelectQuery.SelectWhere<Order> where3 = query.where(o -> o.getDescription().trim().equals(order.getDescription().trim()));

```
lambdaql은 의 where 절과 on 절은 다음과 같은 규칙을 따릅니다.

1. where 절의 람다 함수의 인자는 가상의 entity class 객체 입니다. 
```java
    SelectQuery.SelectWhere<Order> where1 = query.where(o -> order.isActive());
    String jpql = where1.select(o -> o.getId()).toJPQL();
    // select o.id from Order o where o.active = true
```
위 예시에서 람다 함수 인자 o는 실제 Order 인스턴스가 아닌 가상의 Order Entity 객체입니다.
2. 가상의 entity class 객체의 메서드는 쿼리의 컬럼으로 해석되며 실행 시간에 호출되지 않습니다.   
   (가상의 객체이므로 호출되어도 정상적인 결과를 얻을 수 없습니다.)    
   entity class 객체의 메서드가 아닌 람다 함수의 인자에 있는 메서드는 실행 시간에 호출됩니다.

```java
    SelectQuery.SelectWhere<Order> where2 = query.where(o -> o.getDescription().trim() == order.getDescription().trim());
    String jpql = where2.select(o -> o).toJPQL();
    // jpql: select o from Order o where TRIM(o.description) = ?1 
    // select o.* from Order o where TRIM(o.description) = 'test'
    
```
   entity class 객체의 메서드 중 DB 함수로 변환할 수 있는 메서드는 DB 함수로 변환됩니다.
   entity class 를 포함하지 않는 변수나 메서드는 실행 시간에 평가되며 jpql에 포함되지 않습니다.
   위 코드는 아래 JPA Query 와 동일합니다.
```java
    TypedQuery<Order> query = em.createQuery(
        "select o from Order o where TRIM(o.description) = ?1", Order.class);
    Order orderResult = query.setParameter(1, order.getDescription().trim()).getSingleResult();
```
3. 
