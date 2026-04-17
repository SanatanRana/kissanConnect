package com.kisanconnect.product.service;

import com.kisanconnect.product.entity.Review;
import com.kisanconnect.product.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    // ========== Add Review ==========
    public Review addReview(Review review) {
        // One customer can only review a product once
        if (reviewRepository.existsByProductIdAndCustomerId(review.getProductId(), review.getCustomerId())) {
            throw new RuntimeException("You have already reviewed this product");
        }

        review.setReviewDate(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    // ========== Get Reviews for a Product ==========
    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    // ========== Get Reviews by Customer ==========
    public List<Review> getReviewsByCustomer(Long customerId) {
        return reviewRepository.findByCustomerId(customerId);
    }

    // ========== Get Average Rating & Count ==========
    public Map<String, Object> getProductRatingSummary(Long productId) {
        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);
        Long totalReviews = reviewRepository.getReviewCountByProductId(productId);

        return Map.of(
                "productId", productId,
                "averageRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0,
                "totalReviews", totalReviews != null ? totalReviews : 0L
        );
    }

    // ========== Delete Review ==========
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new RuntimeException("Review not found with id: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }
}
