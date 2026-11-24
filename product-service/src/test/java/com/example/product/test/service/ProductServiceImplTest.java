
package com.example.product.test.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import com.example.product.service.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductServiceImpl service;

    private Product createProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setDescription("Gaming laptop");
        product.setPrice(new BigDecimal("50000"));
        product.setStock(10);
        return product;
    }

    // 1. Get all products
    @Test
    void testGetAll() {

        List<Product> products = List.of(
                createProduct(),
                createProduct());

        when(repository.findAll()).thenReturn(products);

        List<Product> result = service.getAll();

        assertEquals(2, result.size());
        verify(repository).findAll();
    }

    // 2. Get product by ID
    @Test
    void testGetByIdSuccess() {

        Product product = createProduct();

        when(repository.findById(1L))
                .thenReturn(Optional.of(product));

        Product result = service.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());

        verify(repository).findById(1L);
    }

    // 3. Product not found
    @Test
    void testGetByIdNotFound() {

        when(repository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.getById(100L));

        assertEquals("Product not found: 100", exception.getMessage());
    }

    // 4. Create product
    @Test
    void testCreate() {

        Product product = createProduct();
        product.setId(1L);

        when(repository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product result = service.create(product);

        assertNull(result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(new BigDecimal("50000"), result.getPrice());

        verify(repository).save(product);
    }

    // 5. Update product
    @Test
    void testUpdate() {

        Product existing = createProduct();

        Product updated = new Product();
        updated.setName("Dell Laptop");
        updated.setDescription("Updated laptop");
        updated.setPrice(new BigDecimal("60000"));
        updated.setStock(20);

        when(repository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(repository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product result = service.update(1L, updated);

        assertEquals(1L, result.getId());
        assertEquals("Dell Laptop", result.getName());
        assertEquals("Updated laptop", result.getDescription());
        assertEquals(new BigDecimal("60000"), result.getPrice());
        assertEquals(20, result.getStock());

        verify(repository).findById(1L);
        verify(repository).save(existing);
    }

    // 6. Update product that does not exist
    @Test
    void testUpdateNotFound() {

        when(repository.findById(100L))
                .thenReturn(Optional.empty());

        Product updated = createProduct();

        assertThrows(
                RuntimeException.class,
                () -> service.update(100L, updated));

        verify(repository, never()).save(any(Product.class));
    }

    // 7. Delete product
    @Test
    void testDelete() {

        Product product = createProduct();

        when(repository.findById(1L))
                .thenReturn(Optional.of(product));

        service.delete(1L);

        verify(repository).findById(1L);
        verify(repository).delete(product);
    }

    // 8. Delete product that does not exist
    @Test
    void testDeleteNotFound() {

        when(repository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> service.delete(100L));

        verify(repository, never()).delete(any(Product.class));
    }
}
