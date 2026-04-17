package com.kisanconnect.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Customer who placed the order
    private Long customerId;
    private String customerName;

    // Seller who owns the items
    private Long sellerId;

    // Delivery information
    private String deliveryAddress;
    private String deliveryType;     // LOCAL or NATIONWIDE
    private String deliveryStatus;   // PENDING, SHIPPED, OUT_FOR_DELIVERY, DELIVERED
    
    // Contact information for direct communication
    private String customerPhone;
    private String sellerPhone;
    
    // For village employment - assigning local youth for delivery
    private Long deliveryPartnerId;
    private String deliveryPartnerName;
    private String deliveryPartnerPhone;

    // Order metadata & Revenue Model
    private Double totalAmount;
    private Double commissionAmount; // Platform fee, e.g. 5%
    private Double sellerEarnings;   // Amount paid to farmer/shopkeeper

    private String orderStatus;      // PLACED, CONFIRMED, CANCELLED
    private String paymentStatus;    // PENDING, PAID, FAILED
    private String paymentMethod;    // UPI, COD, CARD

    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items;
}
