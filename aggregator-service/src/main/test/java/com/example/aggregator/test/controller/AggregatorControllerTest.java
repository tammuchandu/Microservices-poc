package com.example.aggregator.test.controller;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.mockito.Answers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "services.user.url=http://user-service",
        "services.product.url=http://product-service",
        "services.order.url=http://order-service"
})
class AggregatorControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    // -------------------------------------------------
    // 1. GET SUMMARY SUCCESS
    // -------------------------------------------------

    @Test
    void testGetSummarySuccess() {

        Object user = """
                {
                    "id": 1,
                    "name": "Chandu",
                    "email": "chandu@gmail.com"
                }
                """;

        Object orders = """
                [
                    {
                        "id": 101,
                        "userId": 1,
                        "status": "PLACED"
                    }
                ]
                """;

        when(restClient.get()
                .uri("http://user-service/api/users/{id}", 1L)
                .retrieve()
                .body(Object.class))
                .thenReturn(user);

        when(restClient.get()
                .uri("http://order-service/api/orders/user/{userId}", 1L)
                .retrieve()
                .body(Object.class))
                .thenReturn(orders);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/summary/1",
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("user"));
        assertTrue(response.getBody().contains("orders"));
    }

    // -------------------------------------------------
    // 2. GET SUMMARY SERVICE UNAVAILABLE
    // -------------------------------------------------

    @Test
    void testGetSummaryServiceUnavailable() {

        when(restClient.get()
                .uri("http://user-service/api/users/{id}", 100L)
                .retrieve()
                .body(Object.class))
                .thenThrow(new RuntimeException("User service unavailable"));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/summary/100",
                String.class
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    // -------------------------------------------------
    // 3. GET PRODUCTS SUCCESS
    // -------------------------------------------------

    @Test
    void testGetProductsSuccess() {

        Object products = """
                [
                    {
                        "id": 1,
                        "name": "Laptop",
                        "price": 50000
                    }
                ]
                """;

        when(restClient.get()
                .uri("http://product-service/api/products")
                .retrieve()
                .body(Object.class))
                .thenReturn(products);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/products",
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Laptop"));
    }

    // -------------------------------------------------
    // 4. GET PRODUCTS SERVICE UNAVAILABLE
    // -------------------------------------------------

    @Test
    void testGetProductsServiceUnavailable() {

        when(restClient.get()
                .uri("http://product-service/api/products")
                .retrieve()
                .body(Object.class))
                .thenThrow(new RuntimeException("Product service unavailable"));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/products",
                String.class
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    // -------------------------------------------------
    // 5. GET DASHBOARD SUCCESS
    // -------------------------------------------------

    @Test
    void testGetDashboardSuccess() {

        Object user = """
                {
                    "id": 1,
                    "name": "Chandu"
                }
                """;

        Object products = """
                [
                    {
                        "id": 1,
                        "name": "Laptop"
                    }
                ]
                """;

        Object orders = """
                [
                    {
                        "id": 101,
                        "userId": 1
                    }
                ]
                """;

        when(restClient.get()
                .uri("http://user-service/api/users/{id}", 1L)
                .retrieve()
                .body(Object.class))
                .thenReturn(user);

        when(restClient.get()
                .uri("http://product-service/api/products")
                .retrieve()
                .body(Object.class))
                .thenReturn(products);

        when(restClient.get()
                .uri("http://order-service/api/orders/user/{id}", 1L)
                .retrieve()
                .body(Object.class))
                .thenReturn(orders);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/dashboard/1",
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("user"));
        assertTrue(response.getBody().contains("products"));
        assertTrue(response.getBody().contains("orders"));
    }

    // -------------------------------------------------
    // 6. GET DASHBOARD SERVICE UNAVAILABLE
    // -------------------------------------------------

    @Test
    void testGetDashboardServiceUnavailable() {

        when(restClient.get()
                .uri("http://user-service/api/users/{id}", 100L)
                .retrieve()
                .body(Object.class))
                .thenThrow(new RuntimeException("User service unavailable"));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/dashboard/100",
                String.class
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }
}