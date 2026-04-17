package com.kisanconnect.order.service;

import com.kisanconnect.order.entity.Order;
import com.kisanconnect.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    @Value("${app.commission.rate}")
    private double commissionRate;

    // ========== Place Order ==========
    public Order placeOrder(Order order) {
        // Fetch customer details from User Service
        @SuppressWarnings("rawtypes")
        java.util.Map customer = restTemplate.getForObject(
                "http://user-service/api/users/" + order.getCustomerId(),
                java.util.Map.class
        );

        if (customer == null) {
            throw new RuntimeException("Customer not found with id: " + order.getCustomerId());
        }

        order.setCustomerName((String) customer.get("name"));
        order.setCustomerPhone((String) customer.get("phone"));

        // Fetch seller details from User Service
        if (order.getSellerId() != null) {
            @SuppressWarnings("rawtypes")
            java.util.Map seller = restTemplate.getForObject(
                    "http://user-service/api/users/" + order.getSellerId(),
                    java.util.Map.class
            );
            if (seller != null) {
                order.setSellerPhone((String) seller.get("phone"));
            }
        }

        // Validate order has items
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new RuntimeException("Order must have at least one item");
        }

        // Calculate total from items
        double total = order.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        // Commission Logic (configurable rate)
        double commission = total * commissionRate;
        double earnings = total - commission;

        order.setTotalAmount(total);
        order.setCommissionAmount(commission);
        order.setSellerEarnings(earnings);

        order.setOrderStatus("PLACED");
        order.setPaymentStatus("PENDING");
        order.setDeliveryStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());

        // Use a list to track which items we've already deducted stock for
        java.util.List<com.kisanconnect.order.entity.OrderItem> processedItems = new java.util.ArrayList<>();
        
        try {
            // Stock Safeguard: Validate and Reduce Stock for each item
            for (com.kisanconnect.order.entity.OrderItem item : order.getItems()) {
                restTemplate.put(
                    "http://product-service/api/products/" + item.getProductId() + "/reduce-stock?quantity=" + item.getQuantity(),
                    null
                );
                processedItems.add(item);
            }
            
            // Try to save the order
            return orderRepository.save(order);
            
        } catch (Exception e) {
            // COMPENSATION LOGIC: If saving fails, we must put the stock back!
            for (com.kisanconnect.order.entity.OrderItem item : processedItems) {
                try {
                    restTemplate.put(
                        "http://product-service/api/products/" + item.getProductId() + "/increase-stock?quantity=" + item.getQuantity(),
                        null
                    );
                } catch (Exception rollbackError) {
                    // Log this or handle critical failure (best effort for now)
                }
            }
            throw new RuntimeException("Finalizing order failed. Stock restored. Error: " + e.getMessage());
        }
    }

    // ========== Get Order by ID ==========
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    // ========== Get Orders by Customer ==========
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    // ========== Get All Orders ==========
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // ========== Update Order Status (PLACED → CONFIRMED → SHIPPED → DELIVERED) ==========
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = getOrderById(orderId);

        // Validate status transition
        String current = order.getOrderStatus();
        if ("CANCELLED".equals(current)) {
            throw new RuntimeException("Cannot update a cancelled order");
        }
        if ("DELIVERED".equals(current)) {
            throw new RuntimeException("Order is already delivered");
        }

        order.setOrderStatus(status);

        // Sync delivery status with order status
        switch (status) {
            case "CONFIRMED" -> order.setDeliveryStatus("PROCESSING");
            case "SHIPPED" -> order.setDeliveryStatus("SHIPPED");
            case "OUT_FOR_DELIVERY" -> order.setDeliveryStatus("OUT_FOR_DELIVERY");
            case "DELIVERED" -> {
                order.setDeliveryStatus("DELIVERED");
                order.setDeliveryDate(LocalDateTime.now());
            }
        }

        return orderRepository.save(order);
    }

    // ========== Cancel Order ==========
    public Order cancelOrder(Long orderId, Long customerId) {
        Order order = getOrderById(orderId);

        // Only the customer who placed the order can cancel it
        if (!order.getCustomerId().equals(customerId)) {
            throw new RuntimeException("You are not authorized to cancel this order");
        }

        // Can only cancel if not already shipped or delivered
        String status = order.getOrderStatus();
        if ("SHIPPED".equals(status) || "DELIVERED".equals(status) || "OUT_FOR_DELIVERY".equals(status)) {
            throw new RuntimeException("Cannot cancel order — it is already " + status);
        }

        order.setOrderStatus("CANCELLED");
        order.setDeliveryStatus("CANCELLED");

        // RESTORE STOCK: Give the items back to the farmer
        for (com.kisanconnect.order.entity.OrderItem item : order.getItems()) {
            try {
                restTemplate.put(
                    "http://product-service/api/products/" + item.getProductId() + "/increase-stock?quantity=" + item.getQuantity(),
                    null
                );
            } catch (Exception e) {
                // Best effort rollback - in production we'd use a queue
            }
        }

        return orderRepository.save(order);
    }

    // ========== Update Payment Status ==========
    public Order updatePaymentStatus(Long orderId, String status) {
        Order order = getOrderById(orderId);
        order.setPaymentStatus(status);
        return orderRepository.save(order);
    }

    // ========== Assign Delivery Partner ==========
    public Order assignDeliveryPartner(Long orderId, Long partnerId, String partnerName) {
        Order order = getOrderById(orderId);
        
        if ("CANCELLED".equals(order.getOrderStatus())) {
            throw new RuntimeException("Cannot assign delivery for a cancelled order");
        }
        
        // Fetch partner details from User Service
        @SuppressWarnings("rawtypes")
        java.util.Map partner = restTemplate.getForObject(
                "http://user-service/api/users/" + partnerId,
                java.util.Map.class
        );

        order.setDeliveryPartnerId(partnerId);
        order.setDeliveryPartnerName(partnerName);
        if (partner != null) {
            order.setDeliveryPartnerPhone((String) partner.get("phone"));
        }
        
        order.setDeliveryStatus("OUT_FOR_DELIVERY");
        order.setOrderStatus("SHIPPED");
        
        return orderRepository.save(order);
    }
}
