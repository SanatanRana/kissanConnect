package com.kisanconnect.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotBlank(message = "Category is required")
    private String category;  // Vegetables, Fruits, Grains, Seeds, Fertilizer, Grocery, etc.

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;     // Farmer/Shopkeeper decides the price

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private String unit;      // kg, quintal, piece, packet

    private Boolean isPerishable; // true for vegetables/fruits (local delivery), false for grains (nationwide)

    private String imageUrl;  // Main photo of the product/crop

    // Seller Information — stores ID from User Service
    @NotNull(message = "Seller ID is required")
    private Long sellerId;

    private String sellerName;     // Denormalized for fast listing display
    private String sellerVillage;
    private String sellerType;     // FARMER or SHOPKEEPER
    private String sellerPhone;
}
