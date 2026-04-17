package com.kisanconnect.product.repository;

import com.kisanconnect.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySellerId(Long sellerId);
    List<Product> findByCategory(String category);
    List<Product> findBySellerType(String sellerType);
    List<Product> findByIsPerishable(Boolean isPerishable);

    // Search by product name or description (Flipkart-like search)
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByKeyword(String keyword);

    // Filter by price range
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
}
