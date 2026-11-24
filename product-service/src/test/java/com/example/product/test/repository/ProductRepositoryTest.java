
package com.example.product.test.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    private Product createProduct() {

        Product product = new Product();

        product.setName("Laptop-" + UUID.randomUUID());
        product.setDescription("Gaming laptop");
        product.setPrice(new BigDecimal("50000"));
        product.setStock(10);

        return product;
    }

    // 1. Save product
    @Test
    void testSaveProduct() {

        Product product = repository.save(createProduct());

        assertNotNull(product.getId());
        assertEquals(new BigDecimal("50000"), product.getPrice());
        assertEquals(10, product.getStock());
    }

    // 2. Find product by ID
    @Test
    void testFindById() {

        Product saved = repository.save(createProduct());

        Optional<Product> result =
                repository.findById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
        assertEquals(saved.getName(), result.get().getName());
    }

    // 3. Find all products
    @Test
    void testFindAll() {

        repository.save(createProduct());
        repository.save(createProduct());

        assertTrue(repository.findAll().size() >= 2);
    }

    // 4. Check product name exists
    @Test
    void testExistsByName() {

        Product saved = repository.save(createProduct());

        assertTrue(repository.existsByName(saved.getName()));
    }

    // 5. Check product name does not exist
    @Test
    void testExistsByNameFalse() {

        assertFalse(repository.existsByName(
                "Unknown-" + UUID.randomUUID()));
    }

    // 6. Update product
    @Test
    void testUpdateProduct() {

        Product saved = repository.save(createProduct());

        saved.setName("Updated Laptop");
        saved.setPrice(new BigDecimal("60000"));
        saved.setStock(20);

        Product updated = repository.save(saved);

        assertEquals("Updated Laptop", updated.getName());
        assertEquals(new BigDecimal("60000"), updated.getPrice());
        assertEquals(20, updated.getStock());
    }

    // 7. Delete product
    @Test
    void testDeleteProduct() {

        Product saved = repository.save(createProduct());

        Long id = saved.getId();

        repository.delete(saved);

        assertFalse(repository.findById(id).isPresent());
    }
}
