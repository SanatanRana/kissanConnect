package com.kisanconnect.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private String customerName;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;       // 1 to 5 stars

    private String qualityFeedback;    // e.g. "Fresh", "Good packaging", "Chemical-free"
    private String freshnessFeedback;  // e.g. "Very fresh", "Average", "Stale"
    private String comment;            // Free text review

    @ElementCollection
    private java.util.List<String> reviewImages;  // Customer uploads photos of received product

    private LocalDateTime reviewDate;
}
