# LambdaQL 문서

> 람다식을 분석하여 JPQL로 변환하는 Query DSL

---

## 📘 개요: LambdaQL 이란?

**LambdaQL**은 Java의 람다식을 분석하여 **JPQL** 또는 **SQL**로 변환하는 DSL(Query Domain-Specific Language)입니다.  
전체 구성은 다음과 같습니다:

### 🔧 구성 요소

1. **쿼리 빌더 DSL**
2. **람다 분석 및 메서드 분석기**
3. **쿼리 결과를 렌더링하는 Renderer**
   - `JPQLRenderer`: JPQL로 렌더링
   - `SQLRenderer`: SQL로 렌더링
   - `LambdaRenderer`: 디버깅용 복원 렌더러

---

## 📌 핵심 용어 정리

| 용어 | 설명 |
|------|------|
| **Entity 객체** | 람다식의 인자이며, 실제 객체가 아닌 가상의 entity 역할을 함 |
| **Entity 컬럼 메서드** | Entity 객체의 getter 계열 메서드로, JPQL의 컬럼으로 변환됨 |
| **일반 메서드** | Entity가 아닌 외부 값 또는 메서드이며, 런타임에 평가됨 |

---

## 🧪 사용 예제: SelectQuery

```java
long param1 = 70;

Order order = new Order();
order.setId(10L);
order.setDescription(" test ");
order.setActive(true);

Customer customer = new Customer(1, "Bob", List.of(order));

QueryBuilder queryBuilder = new QueryBuilder(entityManagerFactory);
SelectQuery<Order> query = queryBuilder.from(Order.class);

SelectQuery.SelectWhere<Order> where1 = query.where(o -> order.isActive());
SelectQuery.SelectWhere<Order> where2 = query.where(o -> o.getDescription().trim() == order.getDescription().trim());
SelectQuery.SelectWhere<Order> where3 = query.where(o -> o.getDescription().trim().equals(order.getDescription().trim()));
SelectQuery.SelectWhere<Order> where4 = query.where(o -> o.getId() == order.getId()
        && (param1 > 70 ? o.getDescription().trim().equals(order.getDescription().trim()) : null));
```

lambdaql은 의 where 절과 on 절은 다음과 같은 규칙을 따릅니다.

1. where 절의 SQL의 Where 절과 같이 연속된 평가식으로 표현되야 됩니다. 하여 if, swtich, for 문등을 사용할 수 없습니다.
  - 삼항 연산자는 연속된 평가식으로 표현이 가능하므로 사용 가능합니다.
```java
    SelectQuery.SelectWhere<Order> where1 = query.where(o -> order.isActive());
    String jpql = where1.select(o -> o.getId()).toJPQL();
    // select o.id from Order o where o.active = true
```

2.  entity class 객체의 메서드는 쿼리의 컬럼으로 해석되며 실행 시간에 호출되지 않습니다.   
   (가상의 객체이므로 호출되어도 정상적인 결과를 얻을 수 없습니다.)    
   entity 객체의 메서드가 아닌 람다 함수의 인자에 있는 메서드는 실행 시간에 호출됩니다.

```java
    SelectQuery.SelectWhere<Order> where = query.where(o -> o.getDescription() == order.getDescription().trim());
    String jpql = where.select(o -> o).toJPQL();
    // jpql: select o from Order o where o.description = ?1 
    // query: select o.* from Order o where o.description = 'test'
    
```
   entity class 객체의 메서드 중 DB 함수로 변환할 수 있는 메서드는 DB 함수로 변환됩니다.
   entity class 를 포함하지 않는 변수나 메서드는 실행 시간에 평가되며 jpql에 포함되지 않습니다.
   위 코드는 아래 JPA Query 와 동일합니다.
```java
    TypedQuery<Order> query = em.createQuery(
        "select o from Order o where o.description = ?1", Order.class);
    Order orderResult = query.setParameter(1, order.getDescription().trim()).getSingleResult();
```
3. [JpqlFunction] 의 메서드는 Entity 타입을 포함하여 호출될때는 JPQL 함수나 Query 식으로 변환됩니다.   
   - entity class 객체의 메서드가 아닌 람다 함수의 인자에 있는 메서드는 실행 시간에 호출됩니다.
   - 일반 메서드나 변수를 사용할 때는 일반 메서드로 취급하며 실행 시간에 평가됩니다. 
   - JPQL에서 지원하는 함수는 [JpqlFunction] 을 참조하세요.
```java
    SelectQuery.SelectWhere<Order> where3 = query.where(o -> o.getDescription().trim().equals(order.getDescription().trim()));
    String jpql = where3.select(o -> o).toJPQL();
    // jpql: select o from Order o where TRIM(o.description) = ?1 
    // query: select o.* from Order o where TRIM(o.description) = 'test'
```

🔤 STRING 함수
JPQL에서 지원하는 STRING 함수는 다음과 같습니다.

| 함수명     | JPQL 표현식                           | Java 메서드 대응                             |
|------------|----------------------------------------|----------------------------------------------|
| `CONCAT`   | `CONCAT(%s, %s)`                      | `String.concat(String)`                      |
| `LENGTH`   | `LENGTH(%s)`                          | `String.length()`                            |
| `LIKE`     | `%s LIKE %s`                          | `JpqlFunction.like(String, String)`          |
| `LOCATE`   | `LOCATE(%s, %s)`                      | `String.indexOf(String)`                     |
| `LOWER`    | `LOWER(%s)`                           | `String.toLowerCase()`                       |
| `UPPER`    | `UPPER(%s)`                           | `String.toUpperCase()`                       |
| `TRIM`     | `TRIM(%s)`                            | `JpqlFunction.trim(String)`                  |
| `LTRIM`    | `TRIM(LEADING FROM %s)`               | `JpqlFunction.ltrim(String)`                 |
| `RTRIM`    | `TRIM(TRAILING FROM %s)`              | `JpqlFunction.rtrim(String)`                 |
| `LTRIM`    | `TRIM(LEADING '%s' FROM %s)`          | `JpqlFunction.ltrim(char, String)`           |
| `RTRIM`    | `TRIM(TRAILING '%s' FROM %s)`         | `JpqlFunction.rtrim(char, String)`           |
| `TRIM`     | `TRIM(BOTH '%s' FROM %s)`             | `JpqlFunction.trim(char, String)`            |
| `SUBSTRING`| `SUBSTRING(%s, %s, %s)`               | `JpqlFunction.substring(String, int, int)`   |

🔢 NUMERIC 함수
JPQL에서 지원하는 NUMERIC 함수는 다음과 같습니다.

| 함수명     | JPQL 표현식         | Java 메서드 대응                           |
|------------|----------------------|--------------------------------------------|
| `ABS`      | `ABS(%s)`            | `Math.abs`, `JpqlFunction.abs(Number)`     |
| `CEIL`     | `CEIL(%s)`           | `Math.ceil(double)`                        |
| `EXP`      | `EXP(%s)`            | `Math.exp(double)`                         |
| `FLOOR`    | `FLOOR(%s)`          | `Math.floor(double)`                       |
| `LOG`      | `LOG(%s)`            | `Math.log(double)`                         |
| `MOD`      | `MOD(%s, %s)`        | `Math.floorMod`, `JpqlFunction.mod(int,int)`|
| `POWER`    | `POWER(%s, %s)`      | `Math.pow(double,double)`                  |
| `ROUND`    | `ROUND(%s)`          | `Math.round(double)`                       |
| `SIGN`     | `SIGN(%s)`           | `Math.signum(double)`                      |
| `SQRT`     | `SQRT(%s)`           | `Math.sqrt(double)`, `JpqlFunction.sqrt()` |

📅 DATETIME 함수
JPQL에서 지원하는 DATETIME 함수는 다음과 같습니다.

| 함수명           | JPQL 표현식              | Java 메서드 대응                                  |
|------------------|---------------------------|---------------------------------------------------|
| `CURRENT_DATE`   | `CURRENT_DATE`           | `LocalDate.now()`, `OffsetDateTime.now()` 등     |
| `CURRENT_TIME`   | `CURRENT_TIME`           | `LocalTime.now()`, `OffsetDateTime.now()` 등     |
| `CURRENT_TIMESTAMP` | `CURRENT_TIMESTAMP`   | `LocalDateTime.now()`, `Instant.now()` 등        |
| `YEAR`           | `YEAR(%s)`               | `getYear()` from various Date/Time types         |
| `MONTH`          | `MONTH(%s)`              | `getMonthValue()`                                |
| `DAY`            | `DAY(%s)`                | `getDayOfMonth()`                                |
| `HOUR`           | `HOUR(%s)`               | `getHour()`                                       |
| `MINUTE`         | `MINUTE(%s)`             | `getMinute()`                                     |
| `SECOND`         | `SECOND(%s)`             | `getSecond()`                                     |
| `EXTRACT`        | `EXTRACT(%s FROM %s)`    | `JpqlFunction.extract(String, Temporal)`          |

🧮 COLLECTION 함수
JPQL에서 지원하는 COLLECTION 함수는 다음과 같습니다.

| 함수명         | JPQL 표현식            | Java 메서드 대응                     |
|----------------|-------------------------|--------------------------------------|
| `IN`           | `%s IN (%s)`            | `Collection.contains(Object)`        |
| `NOT IN`       | `%s NOT IN (%s)`        | `Collection.contains(Object)`        |
| `INDEX`        | `INDEX(%s)`             | `List.indexOf(Object)`              |
| `IS EMPTY`     | `%s IS EMPTY`           | `Collection.isEmpty()`              |
| `IS NOT EMPTY` | `%s IS NOT EMPTY`       | `Collection.isEmpty()` (논리 반전)   |
| `KEY`          | `KEY(%s)`               | `Map.keySet()`                      |
| `SIZE`         | `SIZE(%s)`              | `Collection.size()`, `Array.length` |

⚙️ OPERATOR 함수
JPQL에서 지원하는 OPERATOR 함수는 다음과 같습니다.

| 함수명       | JPQL 표현식                  | Java 메서드 대응                                      |
|--------------|-------------------------------|-------------------------------------------------------|
| `ALL`        | `%s = ALL(%s)`                | `JpqlFunction.all(Object)`                            |
| `ANY`        | `%s = ANY(%s)`                | `JpqlFunction.any(Object)`                            |
| `CAST`       | `CAST(%s AS %s)`              | `JpqlFunction.cast(Object, Class)`                    |
| `COALESCE`   | `COALESCE(%s)`                | `JpqlFunction.coalesce(Object...)`                    |
| `EXISTS`     | `EXISTS(%s)`                  | `JpqlFunction.exists(Object)`                         |
| `IF`         | `FUNCTION('IF', %s, %s, %s)`  | `JpqlFunction.ifFunc(Object, Object, Object)`         |
| `IFNULL`     | `IFNULL(%s, %s)`              | `JpqlFunction.ifNull(Object, Object)`                 |
| `IS NULL`    | `%s IS NULL`                  | `JpqlFunction.isNull(Object)`                         |
| `IS NOT NULL`| `%s IS NOT NULL`              | `JpqlFunction.isNotNull(Object)`                      |
| `NULLIF`     | `NULLIF(%s, %s)`              | `JpqlFunction.nullIf(Object, Object)`                 |
| `BETWEEN`    | `%s BETWEEN %s AND %s`        | `JpqlFunction.between(Comparable, Comparable, Comparable)` |