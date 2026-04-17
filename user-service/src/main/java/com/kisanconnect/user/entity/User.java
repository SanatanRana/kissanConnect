package com.kisanconnect.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String phone;
    private String address;
    private String village;

    @Enumerated(EnumType.STRING)
    private Role role;   // CUSTOMER, SELLER, ADMIN

    @Enumerated(EnumType.STRING)
    private SellerType sellerType; // FARMER, SHOPKEEPER (only if role is SELLER)

}
