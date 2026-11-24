package com.example.aggregator.test.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;

@SpringBootTest(properties = {
        "services.user.url=http://localhost:8081",
        "services.product.url=http://localhost:8082",
        "services.order.url=http://localhost:8083"
})
class RestClientConfigTest {

    @Autowired
    private RestClient restClient;

    @Test
    void testRestClientBeanCreation() {

        assertNotNull(restClient);
    }
}