package com.kisanconnect.order.service;

import com.kisanconnect.order.dto.CartItemRequest;
import com.kisanconnect.order.entity.Cart;
import com.kisanconnect.order.entity.CartItem;
import com.kisanconnect.order.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    public Cart getCartByCustomerId(Long customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> createEmptyCart(customerId));
    }

    private Cart createEmptyCart(Long customerId) {
        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        cart.setTotalAmount(0.0);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart addItemToCart(Long customerId, CartItemRequest request) {
        Cart cart = getCartByCustomerId(customerId);

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            // Optionally, update price if it dynamically changes, though typical carts use current snapshot
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductId(request.getProductId());
            newItem.setProductName(request.getProductName());
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(request.getPrice());
            newItem.setUnit(request.getUnit());
            cart.getItems().add(newItem);
        }

        recalculateTotal(cart);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItemFromCart(Long customerId, Long productId) {
        Cart cart = getCartByCustomerId(customerId);

        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        
        if (removed) {
            recalculateTotal(cart);
            return cartRepository.save(cart);
        }
        return cart;
    }

    @Transactional
    public void clearCart(Long customerId) {
        Cart cart = getCartByCustomerId(customerId);
        cart.getItems().clear();
        cart.setTotalAmount(0.0);
        cartRepository.save(cart);
    }

    private void recalculateTotal(Cart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        cart.setTotalAmount(total);
    }
}
