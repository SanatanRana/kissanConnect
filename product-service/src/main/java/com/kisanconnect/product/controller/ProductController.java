package com.kisanconnect.product.controller;

import com.kisanconnect.product.entity.Product;
import com.kisanconnect.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // ========== Add Product (Farmer or Shopkeeper lists their item) ==========
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> addProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "image", required = false) org.springframework.web.multipart.MultipartFile image) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Product product = mapper.readValue(productJson, Product.class);

            if (image != null && !image.isEmpty()) {
                String fileName = java.util.UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                if (!java.nio.file.Files.exists(uploadPath)) {
                    java.nio.file.Files.createDirectories(uploadPath);
                }
                java.nio.file.Path filePath = uploadPath.resolve(fileName);
                java.nio.file.Files.copy(image.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                product.setImageUrl("/uploads/" + fileName);
            }

            Product saved = productService.addProduct(product);
            return ResponseEntity.ok(Map.of(
                    "message", "Product listed successfully",
                    "productId", saved.getId(),
                    "imageUrl", saved.getImageUrl() != null ? saved.getImageUrl() : "No image uploaded"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Update Product (Farmer/Shopkeeper updates price, stock, etc. with Image) ==========
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateProductMultipart(
            @PathVariable Long id, 
            @RequestPart("product") String productJson,
            @RequestPart(value = "image", required = false) org.springframework.web.multipart.MultipartFile image) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Product product = mapper.readValue(productJson, Product.class);

            if (image != null && !image.isEmpty()) {
                String fileName = java.util.UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                if (!java.nio.file.Files.exists(uploadPath)) {
                    java.nio.file.Files.createDirectories(uploadPath);
                }
                java.nio.file.Path filePath = uploadPath.resolve(fileName);
                java.nio.file.Files.copy(image.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                product.setImageUrl("/uploads/" + fileName);
            }

            Product updated = productService.updateProduct(id, product);
            return ResponseEntity.ok(Map.of(
                    "message", "Product updated successfully",
                    "productId", updated.getId(),
                    "imageUrl", updated.getImageUrl() != null ? updated.getImageUrl() : "No image uploaded"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Update Product (Farmer/Shopkeeper updates price, stock, etc. without Image) ==========
    @PutMapping(value = "/{id}", consumes = {"application/json"})
    public ResponseEntity<?> updateProductJson(@PathVariable Long id, @RequestBody Product productBody) {
        try {
            Product updated = productService.updateProduct(id, productBody);
            return ResponseEntity.ok(Map.of(
                    "message", "Product updated successfully",
                    "productId", updated.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Delete Product ==========
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, @RequestParam Long sellerId) {
        try {
            productService.deleteProduct(id, sellerId);
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Get All Products ==========
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // ========== Get Product by ID ==========
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.getProductById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Search Products (Flipkart-like keyword search) ==========
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String keyword) {
        return productService.searchProducts(keyword);
    }

    // ========== Filter by Price Range ==========
    @GetMapping("/price")
    public List<Product> getProductsByPriceRange(@RequestParam Double min, @RequestParam Double max) {
        return productService.getProductsByPriceRange(min, max);
    }

    // ========== Get Products by Seller ==========
    @GetMapping("/seller/{sellerId}")
    public List<Product> getProductsBySeller(@PathVariable Long sellerId) {
        return productService.getProductsBySeller(sellerId);
    }

    // ========== Get Products by Category ==========
    @GetMapping("/category/{category}")
    public List<Product> getProductsByCategory(@PathVariable String category) {
        return productService.getProductsByCategory(category);
    }

    // ========== Get Only Farmer Products ==========
    @GetMapping("/farmers")
    public List<Product> getFarmerProducts() {
        return productService.getFarmerProducts();
    }

    // ========== Get Only Shopkeeper Products ==========
    @GetMapping("/shopkeepers")
    public List<Product> getShopkeeperProducts() {
        return productService.getShopkeeperProducts();
    }

    // ========== Local Delivery Items (Perishable) ==========
    @GetMapping("/local")
    public List<Product> getLocalDeliveryProducts() {
        return productService.getPerishableProducts();
    }

    // ========== Nationwide Delivery Items (Non-Perishable) ==========
    @GetMapping("/nationwide")
    public List<Product> getNationwideDeliveryProducts() {
        return productService.getNonPerishableProducts();
    }


    // ========== AI Price Suggestion (Mock for MVP) ==========
    @GetMapping("/suggest-price")
    public ResponseEntity<?> suggestPrice(
            @RequestParam String category) {
        // In a real app, this would call an AI/ML Python microservice.
        // For MVP, returning a mocked AI suggestion based on category & region.
        
        double minPrice = 0;
        double maxPrice = 0;
        String unit = "kg";
        
        switch (category.toLowerCase()) {
            case "wheat": case "grains":
                minPrice = 2200.0; maxPrice = 2500.0; unit = "quintal"; break;
            case "tomato": case "vegetables":
                minPrice = 15.0; maxPrice = 25.0; unit = "kg"; break;
            case "seeds":
                minPrice = 150.0; maxPrice = 300.0; unit = "packet"; break;
            default:
                minPrice = 100.0; maxPrice = 500.0; unit = "unit"; break;
        }
        
        return ResponseEntity.ok(Map.of(
            "message", "AI Suggested Price derived from local Mandi and demand data",
            "category", category,
            "suggestedMinPrice", minPrice,
            "suggestedMaxPrice", maxPrice,
            "unit", unit
        ));
    }

    // ========== Reduce Stock (called by Order Service) ==========
    @PutMapping("/{id}/reduce-stock")
    public ResponseEntity<?> reduceStock(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            productService.reduceStock(id, quantity);
            return ResponseEntity.ok(Map.of("message", "Stock updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Increase Stock (called by Order Service on cancellation) ==========
    @PutMapping("/{id}/increase-stock")
    public ResponseEntity<?> increaseStock(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            productService.increaseStock(id, quantity);
            return ResponseEntity.ok(Map.of("message", "Stock restored successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
