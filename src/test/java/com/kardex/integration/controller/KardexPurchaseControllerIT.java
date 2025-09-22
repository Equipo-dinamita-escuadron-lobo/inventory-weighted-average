package com.kardex.integration.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;
import com.kardex.infrastructure.adapters.output.jpa.repository.IKardexRepository;
import com.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository;

/**
 * Integration tests for the Kardex purchase endpoint
 * Tests focus on validating proper request handling, validation,
 * and response structure for the kardex purchase operation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ImportAutoConfiguration(exclude = {
    RabbitAutoConfiguration.class
})
@ActiveProfiles("test")
@WithMockUser(username = "test-user", roles = {"admin_client"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KardexPurchaseControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private  IProductRepository productRepository;

    @Autowired
    private IKardexRepository kardexRepository;

    private static final String BASE_URL = "/api/kardex/weighted-average/purchase";

    @BeforeAll
    void setUpAll() {
        // Clear existing products
        productRepository.deleteAll();

        ProductEntity product = new ProductEntity();
        product.setProductId(1L);
        product.setReference("REF001");
        product.setName("Test Product");
        product.setPresentation("Box");
        product.setEnterpriseId("ENT123");
        product.setState(true);
        productRepository.save(product);

        ProductEntity secondProduct = new ProductEntity();
        secondProduct.setProductId(2L);
        secondProduct.setReference("REF002");
        secondProduct.setName("Second Product");
        secondProduct.setPresentation("Unit");
        secondProduct.setEnterpriseId("ENT123");
        secondProduct.setState(true);
        productRepository.save(secondProduct);
    }


    @BeforeEach
    void setUp() {
        kardexRepository.deleteAll();
    }

    @Nested
    @DisplayName("Purchase Kardex Tests")
    class PurchaseKardexTests {

        @Test
        @DisplayName("Should successfully register a purchase with all valid fields")
        void shouldRegisterPurchaseWithValidFields() throws Exception {
            // Given: A valid purchase request
            String requestBody = """
                {
                    "quantity": 10,
                    "factCode": 12345,
                    "unitPrice": 100.0,
                    "details": "Purchase details",
                    "productId": 1
                }
                """;

            // When/Then: The request is processed successfully
            mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Kardex purchase registered successfully"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.quantity").value(10))
                .andExpect(jsonPath("$.data.unitPrice").value(100.0))
                .andExpect(jsonPath("$.data.details").value("Purchase details"))
                .andExpect(jsonPath("$.data.type").value("PURCHASE"))
                .andExpect(jsonPath("$.data.balanceQuantity").value(10))
                .andExpect(jsonPath("$.data.balanceUnitPrice").value(100.0))
                .andExpect(jsonPath("$.data.totalBalance").value(1000.0))
                .andExpect(jsonPath("$.data.date").exists());
        }

        @Test
        @DisplayName("Should successfully register a purchase with quantity and verify calculations")
        void shouldRegisterPurchaseWithQuantity() throws Exception {
            // Given: A purchase request with a quantity
            String requestBody = """
                {
                    "quantity": 1000,
                    "factCode": 54321,
                    "unitPrice": 50.75,
                    "details": "Bulk purchase",
                    "productId": 1
                }
                """;

            // When/Then: The calculations should be accurate
            mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(1000))
                .andExpect(jsonPath("$.data.unitPrice").value(50.75))
                .andExpect(jsonPath("$.data.balanceQuantity").value(1000))
                .andExpect(jsonPath("$.data.balanceUnitPrice").value(50.75))
                .andExpect(jsonPath("$.data.totalBalance").value(50750.0));
        }

        @Test
        @DisplayName("Should successfully register multiple purchases and verify weighted average")
        void testMultiplePurchasesWeightedAverage() throws Exception {
            // Given: First purchase
            String firstPurchase = """
                {
                    "quantity": 10,
                    "factCode": 11111,
                    "unitPrice": 10.0,
                    "details": "First purchase",
                    "productId": 1
                }
                """;

            // When: First purchase is processed
            mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstPurchase)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balanceQuantity").value(10))
                .andExpect(jsonPath("$.data.balanceUnitPrice").value(10.0))
                .andExpect(jsonPath("$.data.totalBalance").value(100.0));


            // Given: Second purchase with different price
            String secondPurchase = """
                {
                    "quantity": 10,
                    "factCode": 22222,
                    "unitPrice": 20.0,
                    "details": "Second purchase",
                    "productId": 1
                }
                """;

            // When/Then: Verificamos que la respuesta es correcta sin depender de valores específicos
            mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondPurchase)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balanceQuantity").value(20)) // 10 previous + 10 new
                .andExpect(jsonPath("$.data.balanceUnitPrice").value(15.0)) // 300/20 average
                .andExpect(jsonPath("$.data.totalBalance").value(300.0));
        }


        @Test
        @DisplayName("Should reject request when quantity is missing")
        void shouldFailWhenQuantityMissing() throws Exception {
            // Test for specific required field - quantity
            String body = """
                {
                    "factCode": 12345,
                    "unitPrice": 100.0,
                    "details": "Missing quantity",
                    "productId": 1
                }
                """;

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request when factCode is missing")
        void shouldFailWhenFactCodeMissing() throws Exception {
            // Test for specific required field - factCode
            String body = """
                {
                    "quantity": 10,
                    "unitPrice": 100.0,
                    "details": "Missing factCode",
                    "productId": 1
                }
                """;

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request when unitPrice is missing")
        void shouldFailWhenUnitPriceMissing() throws Exception {
            // Test for specific required field - unitPrice
            String body = """
                {
                    "quantity": 10,
                    "factCode": 12345,
                    "details": "Missing unitPrice",
                    "productId": 1
                }
                """;

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request when productId is missing")
        void shouldFailWhenproductIdMissing() throws Exception {
            // Test for specific required field - productId
            String body = """
                {
                    "quantity": 10,
                    "factCode": 12345,
                    "unitPrice": 100.0,
                    "details": "Missing productId"
                }
                """;

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest());
        }
     
        @Test
        @DisplayName("Should handle purchase with empty details field")
        void testPurchaseKardexWithEmptyDetails() throws Exception {
            // Given: A purchase request with empty details (en lugar de null)
            String requestBody = """
                {
                    "quantity": 5,
                    "factCode": 12345,
                    "unitPrice": 85.50,
                    "details": "",
                    "productId": 1
                }
                """;

            // When/Then: The request should be processed successfully
            mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.details").value(""));
        }
        
        @Test
        @DisplayName("Should reject purchase request for non-existent product")
        void shouldRejectWhenProductNotFound() throws Exception {
            // Given: A purchase request with a non-existent product ID
            String requestBody = """
                {
                    "quantity": 10,
                    "factCode": 12345,
                    "unitPrice": 100.0,
                    "details": "Non-existent product",
                    "productId": 9999
                }
                """;

            // When/Then: The request should be rejected
            mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(status().isNotFound());
        }
        
        @Test
        @DisplayName("Should reject purchase request with negative quantity")
        void shouldRejectWhenQuantityIsNegative() throws Exception {
            // Given: A purchase request with negative quantity
            String requestBody = """
                {
                    "quantity": -5,
                    "factCode": 12345,
                    "unitPrice": 100.0,
                    "details": "Negative quantity",
                    "productId": 1
                }
                """;

            // When/Then: The request should be rejected
            mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Should reject purchase request with negative unit price")
        void shouldRejectWhenUnitPriceIsNegative() throws Exception {
            // Given: A purchase request with negative unit price
            String requestBody = """
                {
                    "quantity": 10,
                    "factCode": 12345,
                    "unitPrice": -50.0,
                    "details": "Negative unit price",
                    "productId": 1
                }
                """;

            // When/Then: The request should be rejected
            mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(status().isBadRequest());
        }
    }
}
