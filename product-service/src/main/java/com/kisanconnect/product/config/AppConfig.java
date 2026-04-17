package com.kisanconnect.product.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    @LoadBalanced   // Uses Eureka service names (e.g., "user-service") instead of hardcoded URLs
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
