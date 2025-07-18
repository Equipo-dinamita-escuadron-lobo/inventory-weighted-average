package com.kardex.infrastructure.adapters.output.jpa.entity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KardexEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private String details;

    @Column(nullable = false)
    private Long balanceQuantity;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal balanceUnitPrice;

    @Column(nullable = false)
    private ZonedDateTime date;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idProduct", nullable = false)
    private ProductEntity product;
}
