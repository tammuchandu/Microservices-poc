package com.example.order.test.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.order.entity.CustomerOrder;
import com.example.order.repository.OrderRepository;

@DataJpaTest
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository repository;

    private CustomerOrder createOrder(Long userId, Long productId) {

        CustomerOrder order = new CustomerOrder();

        order.setUserId(userId);
        order.setProductId(productId);
        order.setProductName("Laptop");
        order.setQuantity(2);
        order.setUnitPrice(new BigDecimal("50000"));
        order.setTotalAmount(new BigDecimal("100000"));
        order.setStatus("PLACED");

        return order;
    }

    @Test
    void testSaveOrder() {

        CustomerOrder order = repository.save(
                createOrder(1L, 10L));

        assertNotNull(order.getId());
        assertEquals(1L, order.getUserId());
        assertEquals("Laptop", order.getProductName());
    }

    @Test
    void testFindAllOrders() {

        repository.save(createOrder(1L, 10L));
        repository.save(createOrder(2L, 20L));

        List<CustomerOrder> orders = repository.findAll();

        assertTrue(orders.size() >= 2);
    }

    @Test
    void testFindById() {

        CustomerOrder saved = repository.save(
                createOrder(1L, 10L));

        Optional<CustomerOrder> result =
                repository.findById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
    }

    @Test
    void testFindByIdNotFound() {

        Optional<CustomerOrder> result =
                repository.findById(-999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByUserId() {

        CustomerOrder saved = repository.save(
                createOrder(1001L, 10L));

        List<CustomerOrder> orders =
                repository.findByUserId(1001L);

        assertFalse(orders.isEmpty());
        assertTrue(orders.stream()
                .allMatch(order -> order.getUserId().equals(1001L)));
    }

    @Test
    void testFindByUserIdNoOrders() {

        List<CustomerOrder> orders =
                repository.findByUserId(-999L);

        assertTrue(orders.isEmpty());
    }

    @Test
    void testUpdateOrder() {

        CustomerOrder saved = repository.save(
                createOrder(1L, 10L));

        saved.setStatus("CANCELLED");

        CustomerOrder updated = repository.save(saved);

        assertEquals("CANCELLED", updated.getStatus());
    }

    @Test
    void testDeleteOrder() {

        CustomerOrder saved = repository.save(
                createOrder(1L, 10L));

        Long id = saved.getId();

        repository.deleteById(id);

        assertFalse(repository.findById(id).isPresent());
    }

    @Test
    void testExistsById() {

        CustomerOrder saved = repository.save(
                createOrder(1L, 10L));

        assertTrue(repository.existsById(saved.getId()));
        assertFalse(repository.existsById(-999L));
    }

    @Test
    void testCountOrders() {

        long before = repository.count();

        repository.save(createOrder(1L, 10L));
        repository.save(createOrder(2L, 20L));

        long after = repository.count();

        assertEquals(before + 2, after);
    }
}