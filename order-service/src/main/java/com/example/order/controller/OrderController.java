
package com.example.order.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.example.order.dto.PlaceOrderRequest;
import com.example.order.entity.CustomerOrder;
import com.example.order.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    // Get all orders
    @GetMapping
    public List<CustomerOrder> getAll() {
        return service.getAll();
    }

    // Get order by ID
    @GetMapping("/{id}")
    public CustomerOrder getById(@PathVariable("id") Long id) {
        return service.getById(id);
    }

    // Get orders by user ID
    @GetMapping("/user/{userId}")
    public List<CustomerOrder> getByUserId(
            @PathVariable("userId") Long userId) {
        return service.getByUserId(userId);
    }

    // Place a new order
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerOrder placeOrder(
            @Valid @RequestBody PlaceOrderRequest request) {
        return service.placeOrder(request);
    }

    // Cancel an existing order
    @PutMapping("/{id}/cancel")
public CustomerOrder cancelOrder(@PathVariable Long id) {
    return service.cancel(id);
}
}
