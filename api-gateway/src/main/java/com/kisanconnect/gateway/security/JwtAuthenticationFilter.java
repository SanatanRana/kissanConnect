package com.kisanconnect.gateway.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // Routes that don't need authentication
    private final List<String> openEndpoints = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/products", // Usually products are public, tweak if needed
            "/uploads"       // Static files should be public
    );

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Allow CORS Preflight requests to pass through without JWT validation
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // Allow open endpoints
        if (openEndpoints.stream().anyMatch(path::contains)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Enforce token requirement
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("UNAUTHORIZED: Missing or Invalid Authorization Header");
            return;
        }

        try {
            String token = authHeader.substring(7);
            Claims claims = jwtUtil.getClaims(token);
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            // Wrap request to add extracted properties to headers for downstream microservices
            HttpServletRequestWrapper modifiedRequest = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    if ("X-User-Id".equalsIgnoreCase(name)) return userId;
                    if ("X-User-Role".equalsIgnoreCase(name)) return role;
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("X-User-Id".equalsIgnoreCase(name)) return Collections.enumeration(Collections.singletonList(userId));
                    if ("X-User-Role".equalsIgnoreCase(name)) return Collections.enumeration(Collections.singletonList(role));
                    return super.getHeaders(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    List<String> names = Collections.list(super.getHeaderNames());
                    names.add("X-User-Id");
                    names.add("X-User-Role");
                    return Collections.enumeration(names);
                }
            };

            filterChain.doFilter(modifiedRequest, response);

        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("UNAUTHORIZED: Token is expired or invalid");
        }
    }
}
