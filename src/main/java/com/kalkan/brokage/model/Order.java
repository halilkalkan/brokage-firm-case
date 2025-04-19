package com.kalkan.brokage.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;
    private String assetName;

    @Enumerated(EnumType.STRING)
    private Side orderSide;

    private Double size;
    private Double price;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createDate;

    public enum Side {
        BUY, SELL
    }

    public enum Status {
        PENDING, MATCHED, CANCELED
    }
}