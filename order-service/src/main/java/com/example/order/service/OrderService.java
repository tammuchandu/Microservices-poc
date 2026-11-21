
package com.example.order.service;

import java.util.List;

import com.example.order.dto.PlaceOrderRequest;
import com.example.order.entity.CustomerOrder;

public interface OrderService {

    CustomerOrder placeOrder(PlaceOrderRequest request);

    List<CustomerOrder> getAll();

    CustomerOrder getById(Long id);

    List<CustomerOrder> getByUserId(Long userId);

    CustomerOrder cancel(Long id);
}
