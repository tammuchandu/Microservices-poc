
package com.example.order.test.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.example.order.dto.PlaceOrderRequest;
import com.example.order.dto.ProductResponse;
import com.example.order.dto.UserResponse;
import com.example.order.entity.CustomerOrder;
import com.example.order.repository.OrderRepository;
import com.example.order.service.OrderServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository repository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    private OrderServiceImpl service;

    private final String USER_URL = "http://localhost:8081";
    private final String PRODUCT_URL = "http://localhost:8082";

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(
                repository, restClient, USER_URL, PRODUCT_URL);
    }

    private PlaceOrderRequest createRequest() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setUserId(1L);
        request.setProductId(10L);
        request.setQuantity(2);
        return request;
    }

    private UserResponse createUser() {
        return new UserResponse(
                1L, "Chandu", "chandu@gmail.com", "9876543210");
    }

    private ProductResponse createProduct() {
        return new ProductResponse(
                10L, "Laptop", "Gaming laptop",
                new BigDecimal("50000"), 10);
    }

    private CustomerOrder createOrder() {
        CustomerOrder order = new CustomerOrder();
        order.setId(1L);
        order.setUserId(1L);
        order.setProductId(10L);
        order.setProductName("Laptop");
        order.setQuantity(2);
        order.setUnitPrice(new BigDecimal("50000"));
        order.setTotalAmount(new BigDecimal("100000"));
        order.setStatus("PLACED");
        return order;
    }

    // 1. Place order successfully
    @Test
    void testPlaceOrderSuccess() {

        PlaceOrderRequest request = createRequest();

        when(restClient.get()
                .uri(USER_URL + "/api/users/{id}", 1L)
                .retrieve()
                .body(UserResponse.class))
                .thenReturn(createUser());

        when(restClient.get()
                .uri(PRODUCT_URL + "/api/products/{id}", 10L)
                .retrieve()
                .body(ProductResponse.class))
                .thenReturn(createProduct());

        when(repository.save(any(CustomerOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CustomerOrder result = service.placeOrder(request);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(10L, result.getProductId());
        assertEquals("Laptop", result.getProductName());
        assertEquals(2, result.getQuantity());
        assertEquals(new BigDecimal("50000"), result.getUnitPrice());
        assertEquals(new BigDecimal("100000"), result.getTotalAmount());
        assertEquals("PLACED", result.getStatus());

        verify(repository).save(any(CustomerOrder.class));
    }

    // 2. Null request
    @Test
    void testPlaceOrderNullRequest() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.placeOrder(null));

        assertEquals(400, exception.getStatusCode().value());

        verifyNoInteractions(repository, restClient);
    }

    // 3. Invalid request
    @Test
    void testPlaceOrderInvalidRequest() {

        PlaceOrderRequest request = new PlaceOrderRequest();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.placeOrder(request));

        assertEquals(400, exception.getStatusCode().value());

        verifyNoInteractions(repository, restClient);
    }

    // 4. Insufficient product stock
    @Test
    void testPlaceOrderInsufficientStock() {

        PlaceOrderRequest request = createRequest();

        ProductResponse product = new ProductResponse(
                10L, "Laptop", "Gaming laptop",
                new BigDecimal("50000"), 1);

        when(restClient.get()
                .uri(USER_URL + "/api/users/{id}", 1L)
                .retrieve()
                .body(UserResponse.class))
                .thenReturn(createUser());

        when(restClient.get()
                .uri(PRODUCT_URL + "/api/products/{id}", 10L)
                .retrieve()
                .body(ProductResponse.class))
                .thenReturn(product);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.placeOrder(request));

        assertEquals(400, exception.getStatusCode().value());

        verify(repository, never()).save(any(CustomerOrder.class));
    }

    // 5. Get all orders
    @Test
    void testGetAll() {

        List<CustomerOrder> orders = List.of(
                createOrder(),
                createOrder());

        when(repository.findAll()).thenReturn(orders);

        List<CustomerOrder> result = service.getAll();

        assertEquals(2, result.size());

        verify(repository).findAll();
    }

    // 6. Get order by ID successfully
    @Test
    void testGetByIdSuccess() {

        CustomerOrder order = createOrder();

        when(repository.findById(1L))
                .thenReturn(Optional.of(order));

        CustomerOrder result = service.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(repository).findById(1L);
    }

    // 7. Get order by ID - not found
    @Test
    void testGetByIdNotFound() {

        when(repository.findById(100L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getById(100L));

        assertEquals(404, exception.getStatusCode().value());
    }

    // 8. Get orders by user ID
    @Test
    void testGetByUserId() {

        List<CustomerOrder> orders = List.of(createOrder());

        when(repository.findByUserId(1L)).thenReturn(orders);

        List<CustomerOrder> result = service.getByUserId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());

        verify(repository).findByUserId(1L);
    }

    // 9. Cancel order successfully
    @Test
    void testCancelSuccess() {

        CustomerOrder order = createOrder();

        when(repository.findById(1L))
                .thenReturn(Optional.of(order));

        when(repository.save(any(CustomerOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CustomerOrder result = service.cancel(1L);

        assertEquals("CANCELLED", result.getStatus());

        verify(repository).save(order);
    }

    // 10. Cancel already cancelled order
    @Test
    void testCancelAlreadyCancelled() {

        CustomerOrder order = createOrder();
        order.setStatus("CANCELLED");

        when(repository.findById(1L))
                .thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.cancel(1L));

        assertEquals(409, exception.getStatusCode().value());

        verify(repository, never()).save(any(CustomerOrder.class));
    }
}
