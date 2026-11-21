
package com.example.order.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.example.order.dto.PlaceOrderRequest;
import com.example.order.dto.ProductResponse;
import com.example.order.dto.UserResponse;
import com.example.order.entity.CustomerOrder;
import com.example.order.repository.OrderRepository;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final RestClient restClient;
    private final String userServiceUrl;
    private final String productServiceUrl;

    public OrderServiceImpl(
            OrderRepository repository,
            RestClient restClient,
            @Value("${services.user.url}") String userServiceUrl,
            @Value("${services.product.url}") String productServiceUrl) {

        this.repository = repository;
        this.restClient = restClient;
        this.userServiceUrl = userServiceUrl;
        this.productServiceUrl = productServiceUrl;
    }

    // Place a new order
    @Override
    public CustomerOrder placeOrder(PlaceOrderRequest request) {

        if (request == null
                || request.getUserId() == null
                || request.getProductId() == null
                || request.getQuantity() == null
                || request.getQuantity() < 1) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "userId, productId and a positive quantity are required");
        }

        try {

            // Communicate with User Service
            UserResponse user = restClient.get()
                    .uri(userServiceUrl + "/api/users/{id}",
                            request.getUserId())
                    .retrieve()
                    .body(UserResponse.class);

            if (user == null || user.id() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Invalid User Service response");
            }

            // Communicate with Product Service
            ProductResponse product = restClient.get()
                    .uri(productServiceUrl + "/api/products/{id}",
                            request.getProductId())
                    .retrieve()
                    .body(ProductResponse.class);

            if (product == null
                    || product.id() == null
                    || product.price() == null
                    || product.name() == null
                    || product.stock() == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Invalid Product Service response");
            }

            // Check available stock
            if (product.stock() < request.getQuantity()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Insufficient product stock");
            }

            // Create order
            CustomerOrder order = new CustomerOrder();

            order.setUserId(user.id());
            order.setProductId(product.id());
            order.setProductName(product.name());
            order.setQuantity(request.getQuantity());
            order.setUnitPrice(product.price());

            // Calculate total amount
            order.setTotalAmount(
                    product.price().multiply(
                            BigDecimal.valueOf(request.getQuantity())));

            order.setStatus("PLACED");

            return repository.save(order);

        } catch (ResponseStatusException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not validate user or product. Check that User and Product Services are running.",
                    ex);
        }
    }

    // Get all orders
    @Override
    public List<CustomerOrder> getAll() {
        return repository.findAll();
    }

    // Get order by ID
    @Override
    public CustomerOrder getById(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found: " + id));
    }

    // Get orders by user ID
    @Override
    public List<CustomerOrder> getByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    // Cancel order
    @Override
public CustomerOrder cancel(Long id) {

    CustomerOrder order = getById(id);

    if ("CANCELLED".equals(order.getStatus())) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Order is already cancelled"
        );
    }

    order.setStatus("CANCELLED");

    return repository.save(order);
}
}
