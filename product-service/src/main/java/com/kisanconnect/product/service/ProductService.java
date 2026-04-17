package com.kisanconnect.product.service;

import com.kisanconnect.product.entity.Product;
import com.kisanconnect.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;

    // ========== Add Product (Farmer or Shopkeeper) ==========
    public Product addProduct(Product product) {
        // Fetch seller details from User Service
        @SuppressWarnings("rawtypes")
        java.util.Map user = restTemplate.getForObject(
                "http://user-service/api/users/" + product.getSellerId(),
                java.util.Map.class
        );

        if (user == null) {
            throw new RuntimeException("Seller not found with id: " + product.getSellerId());
        }

        // Populate denormalized seller info for fast listing/contact
        product.setSellerName((String) user.get("name"));
        product.setSellerPhone((String) user.get("phone"));
        product.setSellerVillage((String) user.get("village"));
        product.setSellerType((String) user.get("sellerType"));

        return productRepository.save(product);
    }

    // ========== Update Product (Farmer/Shopkeeper updates price, quantity, etc.) ==========
    public Product updateProduct(Long id, Product updatedProduct) {
        Product existing = getProductById(id);

        // Only the same seller can update their product
        if (!existing.getSellerId().equals(updatedProduct.getSellerId())) {
            throw new RuntimeException("You are not authorized to update this product");
        }

        existing.setName(updatedProduct.getName());
        existing.setDescription(updatedProduct.getDescription());
        existing.setCategory(updatedProduct.getCategory());
        existing.setPrice(updatedProduct.getPrice());
        existing.setQuantity(updatedProduct.getQuantity());
        existing.setUnit(updatedProduct.getUnit());
        existing.setIsPerishable(updatedProduct.getIsPerishable());
        existing.setImageUrl(updatedProduct.getImageUrl());

        return productRepository.save(existing);
    }

    // ========== Delete Product ==========
    public void deleteProduct(Long id, Long sellerId) {
        Product product = getProductById(id);

        if (!product.getSellerId().equals(sellerId)) {
            throw new RuntimeException("You are not authorized to delete this product");
        }

        productRepository.deleteById(id);
    }

    // ========== Get All Products ==========
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // ========== Get Product by ID ==========
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    // ========== Get Products by Seller ==========
    public List<Product> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    // ========== Get Products by Category ==========
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    // ========== Search Products (Flipkart-like keyword search) ==========
    public List<Product> searchProducts(String keyword) {
        return productRepository.searchByKeyword(keyword);
    }

    // ========== Filter by Price Range ==========
    public List<Product> getProductsByPriceRange(Double min, Double max) {
        return productRepository.findByPriceBetween(min, max);
    }

    // ========== Get Farmer Products Only ==========
    public List<Product> getFarmerProducts() {
        return productRepository.findBySellerType("FARMER");
    }

    // ========== Get Shopkeeper Products Only ==========
    public List<Product> getShopkeeperProducts() {
        return productRepository.findBySellerType("SHOPKEEPER");
    }

    // ========== Get Perishable (Local Delivery) ==========
    public List<Product> getPerishableProducts() {
        return productRepository.findByIsPerishable(true);
    }

    // ========== Get Non-Perishable (Nationwide Delivery) ==========
    public List<Product> getNonPerishableProducts() {
        return productRepository.findByIsPerishable(false);
    }

    // ========== Reduce Stock (called when an order is placed) ==========
    public void reduceStock(Long id, Integer quantity) {
        Product product = getProductById(id);
        
        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Only " + product.getQuantity() + " units available for " + product.getName() + ". Please review your quantity.");
        }
        
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
    }

    // ========== Increase Stock (called on cancellation or rollback) ==========
    public void increaseStock(Long id, Integer quantity) {
        Product product = getProductById(id);
        product.setQuantity(product.getQuantity() + quantity);
        productRepository.save(product);
    }
}
