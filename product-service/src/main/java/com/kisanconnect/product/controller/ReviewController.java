package com.kisanconnect.product.controller;

import com.kisanconnect.product.entity.Review;
import com.kisanconnect.product.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // ========== Add Review (Customer rates a product) ==========
    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody Review review) {
        try {
            Review saved = reviewService.addReview(review);
            return ResponseEntity.ok(Map.of(
                    "message", "Review added successfully",
                    "reviewId", saved.getId(),
                    "rating", saved.getRating()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Get All Reviews for a Product ==========
    @GetMapping("/product/{productId}")
    public List<Review> getReviewsByProduct(@PathVariable Long productId) {
        return reviewService.getReviewsByProduct(productId);
    }

    // ========== Get Rating Summary (Average + Count) ==========
    @GetMapping("/product/{productId}/summary")
    public Map<String, Object> getRatingSummary(@PathVariable Long productId) {
        return reviewService.getProductRatingSummary(productId);
    }

    // ========== Get Reviews by Customer ==========
    @GetMapping("/customer/{customerId}")
    public List<Review> getReviewsByCustomer(@PathVariable Long customerId) {
        return reviewService.getReviewsByCustomer(customerId);
    }

    // ========== Delete Review ==========
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
