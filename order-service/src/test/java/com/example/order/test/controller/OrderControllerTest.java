
package com.example.order.test.controller;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.order.controller.OrderController;
import com.example.order.dto.PlaceOrderRequest;
import com.example.order.entity.CustomerOrder;
import com.example.order.service.OrderService;

@WebMvcTest(OrderController.class)
class OrderControllerTest {
/*->MockMvc:- is used to send HTTP requests to your Spring Boot Controller 
->during testing without starting a real web server. */
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService service;

    // Create sample order
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
        order.setCreatedAt(LocalDateTime.now());

        return order;
    }

    // 1. Get all orders
    @Test
    void testGetAllOrders() throws Exception {

        List<CustomerOrder> orders = List.of(
                createOrder(),
                createOrder());

        when(service.getAll()).thenReturn(orders);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productName").value("Laptop"))
                .andExpect(jsonPath("$[0].status").value("PLACED"));

        verify(service).getAll();
    }

    // 2. Get order by ID
    @Test
    void testGetOrderById() throws Exception {

        CustomerOrder order = createOrder();

        when(service.getById(1L)).thenReturn(order);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.productName").value("Laptop"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.status").value("PLACED"));

        verify(service).getById(1L);
    }

    // 3. Get orders by user ID
    @Test
    void testGetOrdersByUserId() throws Exception {

        List<CustomerOrder> orders = List.of(createOrder());

        when(service.getByUserId(1L)).thenReturn(orders);

        mockMvc.perform(get("/api/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(1));

        verify(service).getByUserId(1L);
    }

    // 4. Place order successfully
    @Test
    void testPlaceOrderSuccess() throws Exception {

        CustomerOrder order = createOrder();

        when(service.placeOrder(any(PlaceOrderRequest.class)))
                .thenReturn(order);

        String requestJson = """
                {
                    "userId": 1,
                    "productId": 10,
                    "quantity": 2
                }
                """;

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productName").value("Laptop"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalAmount").value(100000));

        verify(service).placeOrder(any(PlaceOrderRequest.class));
    }

    // 5. Place order with invalid data
    @Test
    void testPlaceOrderInvalidData() throws Exception {

        String requestJson = """
                {
                    "userId": null,
                    "productId": 10,
                    "quantity": 0
                }
                """;

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());

        verify(service, never())
                .placeOrder(any(PlaceOrderRequest.class));
    }

    // 6. Cancel order
    @Test
    void testCancelOrder() throws Exception {

        CustomerOrder order = createOrder();
        order.setStatus("CANCELLED");

        when(service.cancel(1L)).thenReturn(order);

        mockMvc.perform(put("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(service).cancel(1L);
    }
}


/*MockMvc → Tests controllers without a real web server.



TestRestTemplate → Sends real HTTP requests to your running test application.



RestClient → Used by your application to communicate with other microservices. */