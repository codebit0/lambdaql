package org.lambdaql.query;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "orders")
@Data
@Getter
@Setter
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name= "product_id")
    private String product;

    private byte[] image;

    private String description;

    private double price;

    private float tex;

    private short status;

    private boolean active;

    private LocalDateTime updateAt;

    private Date createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    public Order() {}

    public Order(Customer customer, String product) {
        this.customer = customer;
        this.product = product;
    }

    public boolean items(int item, int quantity) {
        return item == 1 && quantity > 0;
    }

}
