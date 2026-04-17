package com.kisanconnect.order.controller;

import com.kisanconnect.order.entity.Cart;
import com.kisanconnect.order.entity.Order;
import com.kisanconnect.order.entity.OrderItem;
import com.kisanconnect.order.service.CartService;
import com.kisanconnect.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    // ========== Place Order ==========
    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody Order order) {
        try {
            Order placed = orderService.placeOrder(order);
            return ResponseEntity.ok(Map.of(
                    "message", "Order placed successfully",
                    "orderId", placed.getId(),
                    "totalAmount", placed.getTotalAmount(),
                    "deliveryType", placed.getDeliveryType() != null ? placed.getDeliveryType() : "NOT_SET"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Checkout Cart to Place Order ==========
    @PostMapping("/checkout/{customerId}")
    public ResponseEntity<?> checkoutCart(@PathVariable Long customerId, @RequestBody(required = false) Order orderDetails) {
        try {
            Cart cart = cartService.getCartByCustomerId(customerId);
            if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty. Cannot process checkout."));
            }

            Order order = new Order();
            order.setCustomerId(customerId);
            order.setTotalAmount(cart.getTotalAmount());
            
            if (orderDetails != null) {
                order.setCustomerName(orderDetails.getCustomerName());
                order.setDeliveryAddress(orderDetails.getDeliveryAddress());
                order.setDeliveryType(orderDetails.getDeliveryType());
                order.setCustomerPhone(orderDetails.getCustomerPhone());
                order.setPaymentMethod(orderDetails.getPaymentMethod());
                order.setSellerId(orderDetails.getSellerId());
            }

            List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
                OrderItem item = new OrderItem();
                item.setProductId(cartItem.getProductId());
                item.setProductName(cartItem.getProductName());
                item.setQuantity(cartItem.getQuantity());
                item.setPrice(cartItem.getPrice());
                item.setUnit(cartItem.getUnit());
                return item;
            }).collect(Collectors.toList());

            order.setItems(orderItems);

            Order placedOrder = orderService.placeOrder(order);
            cartService.clearCart(customerId);

            return ResponseEntity.ok(placedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Get Order by ID ==========
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.getOrderById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Get Orders by Customer ==========
    @GetMapping("/customer/{customerId}")
    public List<Order> getOrdersByCustomer(@PathVariable Long customerId) {
        return orderService.getOrdersByCustomer(customerId);
    }

    // ========== Get All Orders ==========
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    // ========== Update Order Status (PLACED → CONFIRMED → SHIPPED → DELIVERED) ==========
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Order updated = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(Map.of(
                    "message", "Order status updated",
                    "orderStatus", updated.getOrderStatus(),
                    "deliveryStatus", updated.getDeliveryStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Cancel Order ==========
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, @RequestParam Long customerId) {
        try {
            Order cancelled = orderService.cancelOrder(id, customerId);
            return ResponseEntity.ok(Map.of(
                    "message", "Order cancelled successfully",
                    "orderStatus", cancelled.getOrderStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Assign Delivery Partner ==========
    @PutMapping("/{id}/assign-delivery")
    public ResponseEntity<?> assignDeliveryPartner(
            @PathVariable Long id, 
            @RequestParam Long partnerId, 
            @RequestParam String partnerName) {
        try {
            Order updated = orderService.assignDeliveryPartner(id, partnerId, partnerName);
            return ResponseEntity.ok(Map.of(
                    "message", "Delivery partner assigned successfully",
                    "deliveryStatus", updated.getDeliveryStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Update Payment Status (PENDING → PAID) ==========
    @PutMapping("/{id}/payment")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Order updated = orderService.updatePaymentStatus(id, status);
            return ResponseEntity.ok(Map.of(
                    "message", "Payment status updated",
                    "paymentStatus", updated.getPaymentStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
