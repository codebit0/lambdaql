CREATE TABLE customers
(
    id INT NOT NULL,
    name        VARCHAR(255),
    create_at   DATETIME,
    PRIMARY KEY (id)
);

CREATE TABLE orders
(
    id          BIGINT NOT NULL AUTO_INCREMENT,
    customer_id INT,
    product     VARCHAR(255),
    PRIMARY KEY (id)
);