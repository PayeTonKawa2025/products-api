package com.paye_ton_kawa.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product")
@Getter
@Setter
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name="name", nullable = false, updatable = true)
    private String name;

    @Column(name="price", nullable = false, updatable = true)
    private double price;

    @Column(name="description", nullable = false, updatable = true)
    private String description;

    @Column(name="color", nullable = true, updatable = true)
    private String color;

    @Column(name="stock", nullable = false, updatable = true)
    private int stock;

}