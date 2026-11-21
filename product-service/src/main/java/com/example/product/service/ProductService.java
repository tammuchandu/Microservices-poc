
package com.example.product.service;

import java.util.List;
import com.example.product.entity.Product;

public interface ProductService {

    List<Product> getAll();

    Product getById(Long id);

    Product create(Product product);

    Product update(Long id, Product product);

    void delete(Long id);
}
