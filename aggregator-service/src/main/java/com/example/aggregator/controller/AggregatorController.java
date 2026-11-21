package com.example.aggregator.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class AggregatorController {
    private final RestClient restClient;
    private final String userServiceUrl;
    private final String productServiceUrl;
    private final String orderServiceUrl;

    public AggregatorController(RestClient restClient,
            @Value("${services.user.url}") String userServiceUrl,
            @Value("${services.product.url}") String productServiceUrl,
            @Value("${services.order.url}") String orderServiceUrl) {
        this.restClient = restClient;
        this.userServiceUrl = userServiceUrl;
        this.productServiceUrl = productServiceUrl;
        this.orderServiceUrl = orderServiceUrl;
    }

    @GetMapping("/summary/{userId}")
    public Map<String, Object> getSummary(@PathVariable Long userId) {
        try {
            Object user = restClient.get()
                    .uri(userServiceUrl + "/api/users/{id}", userId)
                    .retrieve().body(Object.class);
            Object orders = restClient.get()
                    .uri(orderServiceUrl + "/api/orders/user/{userId}", userId)
                    .retrieve().body(Object.class);
            return Map.of("user", user, "orders", orders);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not retrieve summary. Check User and Order Services.", ex);
        }
    }

    @GetMapping("/products")
    public Object getProducts() {
        try {
            return restClient.get().uri(productServiceUrl + "/api/products")
                    .retrieve().body(Object.class);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not retrieve products. Check Product Service.", ex);
        }
    }

    @GetMapping("/dashboard/{userId}")
    public Map<String, Object> getDashboard(@PathVariable Long userId) {
        try {
            Object user = restClient.get()
                    .uri(userServiceUrl + "/api/users/{id}", userId)
                    .retrieve().body(Object.class);
            Object products = restClient.get()
                    .uri(productServiceUrl + "/api/products")
                    .retrieve().body(Object.class);
            Object orders = restClient.get()
                    .uri(orderServiceUrl + "/api/orders/user/{id}", userId)
                    .retrieve().body(Object.class);
            return Map.of("user", user, "products", products, "orders", orders);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not retrieve dashboard. Check all three business services.", ex);
        }
    }
}
