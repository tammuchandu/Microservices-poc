
package com.example.product.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Product> getAll() {
        return repository.findAll();
    }

    @Override
    public Product getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Product not found: " + id));
    }

    @Override
    public Product create(Product product) {
        product.setId(null);
        return repository.save(product);
    }

    @Override
    public Product update(Long id, Product product) {
        Product existing = getById(id);

        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());

        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Product existing = getById(id);
        repository.delete(existing);
    }
}
