package com.kisanconnect.order.repository;

import com.kisanconnect.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByOrderStatus(String orderStatus);
    List<Order> findByDeliveryType(String deliveryType);
}
