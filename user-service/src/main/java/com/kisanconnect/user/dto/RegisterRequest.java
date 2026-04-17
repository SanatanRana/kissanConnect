package com.kisanconnect.user.dto;

import com.kisanconnect.user.entity.Role;
import com.kisanconnect.user.entity.SellerType;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
    private String address;
    private String village;
    private Role role;
    private SellerType sellerType;
}
