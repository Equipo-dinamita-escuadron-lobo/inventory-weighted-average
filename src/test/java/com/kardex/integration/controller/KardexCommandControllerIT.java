package com.kardex.integration.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;
import com.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ImportAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration.class
})
@ActiveProfiles("test")
class KardexCommandControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private  IProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        ProductEntity product = new ProductEntity();
        product.setIdProduct(1L);
        product.setReference("REF001");
        product.setName("Test Product");
        product.setPresentation("Box");
        product.setManager("John Doe");
        product.setEnterpriseId("ENT123");
        product.setState(true);
        productRepository.save(product);

    }

    @Test
    @WithMockUser(username = "test-user", roles = {"admin_client"})
    void testPurchaseKardex() throws Exception {
        String requestBody = """
            {
                "quantity": 10,
                "factCode": 12345,
                "unitPrice": 100.0,
                "details": "Purchase details",
                "idProduct": 1
            }
            """;

        mockMvc.perform(
                post("/api/kardex/weighted-average/purchase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
            .andDo(print())
            .andExpect(status().isOk());
    }
}
