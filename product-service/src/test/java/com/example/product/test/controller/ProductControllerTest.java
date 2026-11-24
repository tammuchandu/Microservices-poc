
package com.example.product.test.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.product.controller.ProductController;
import com.example.product.entity.Product;
import com.example.product.service.ProductService;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService service;

    private Product createProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setDescription("Gaming laptop");
        product.setPrice(new BigDecimal("50000"));
        product.setStock(10);
        return product;
    }

    // 1. GET all products
    @Test
    void testGetAll() throws Exception {

        when(service.getAll())
                .thenReturn(List.of(createProduct()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"));

        verify(service).getAll();
    }

    // 2. GET product by ID
    @Test
    void testGetById() throws Exception {

        when(service.getById(1L))
                .thenReturn(createProduct());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(service).getById(1L);
    }

    // 3. POST create product
    @Test
    void testCreate() throws Exception {

        when(service.create(any(Product.class)))
                .thenReturn(createProduct());

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Laptop",
                            "description": "Gaming laptop",
                            "price": 50000,
                            "stock": 10
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(service).create(any(Product.class));
    }

    // 4. POST with invalid product
    @Test
    void testCreateInvalidProduct() throws Exception {

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "",
                            "price": 0,
                            "stock": -1
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    // 5. PUT update product
    @Test
    void testUpdate() throws Exception {

        when(service.update(eq(1L), any(Product.class)))
                .thenReturn(createProduct());

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Laptop",
                            "description": "Gaming laptop",
                            "price": 50000,
                            "stock": 10
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(service).update(eq(1L), any(Product.class));
    }

    // 6. DELETE product
    @Test
    void testDelete() throws Exception {

        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }
}
