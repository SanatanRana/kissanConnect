package com.kisanconnect.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private String unit;
}
