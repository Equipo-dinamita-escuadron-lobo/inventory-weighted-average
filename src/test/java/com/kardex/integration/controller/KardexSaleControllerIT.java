package com.kardex.integration.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.math.BigDecimal;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.kardex.domain.model.MovementType;
import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;
import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;
import com.kardex.infrastructure.adapters.output.jpa.repository.IKardexRepository;
import com.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository;

/**
 * Integration tests for the Kardex sale endpoint
 * Tests focus on validating proper request handling, validation,
 * and response structure for the kardex sale operation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ImportAutoConfiguration(exclude = {
    RabbitAutoConfiguration.class
})
@ActiveProfiles("test")
@WithMockUser(username = "test-user", roles = {"admin_client"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KardexSaleControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private  IProductRepository productRepository;

    @Autowired
    private IKardexRepository kardexRepository;

    private static final String BASE_URL = "/api/kardex/weighted-average/sale";

    @BeforeAll
    void setUpAll() {
        // Clear existing products
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

        ProductEntity secondProduct = new ProductEntity();
        secondProduct.setIdProduct(2L);
        secondProduct.setReference("REF002");
        secondProduct.setName("Second Product");
        secondProduct.setPresentation("Unit");
        secondProduct.setManager("Jane Smith");
        secondProduct.setEnterpriseId("ENT123");
        secondProduct.setState(true);
        productRepository.save(secondProduct);
    }


    @BeforeEach
    void setUp() {
        kardexRepository.deleteAll();

        KardexEntity kardexEntry = new KardexEntity();
        kardexEntry.setFactCode(1L);
        kardexEntry.setQuantity(10L);
        kardexEntry.setUnitPrice(BigDecimal.valueOf(10.0));
        kardexEntry.setDetails("Initial stock");
        kardexEntry.setType(MovementType.PURCHASE);
        kardexEntry.setBalanceQuantity(10L);
        kardexEntry.setBalanceUnitPrice(BigDecimal.valueOf(10.0));
        kardexEntry.setTotalBalance(BigDecimal.valueOf(100.0));
        kardexEntry.setDate(java.time.ZonedDateTime.now());
        kardexEntry.setIdProduct(1L);
        kardexRepository.save(kardexEntry);
    }

    @Nested
    @DisplayName("Sale Kardex Tests")
    class SaleKardexTests {

        @Test
        @DisplayName("Should successfully register a sale with all valid fields")
        void shouldRegisterSaleWithValidFields() throws Exception {
            // Given: A valid sale request
            String requestBody = """
                {
                    "quantity": 5,
                    "factCode": 2,
                    "details": "Sale of 5 units",
                    "idProduct": 1
                }
            """;

            //When/Then: the request is processed successfully
            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("Kardex sale registered successfully"))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.quantity").value(5))
            .andExpect(jsonPath("$.data.unitPrice").value(10.0))
            .andExpect(jsonPath("$.data.details").value("Sale of 5 units"))
            .andExpect(jsonPath("$.data.type").value("SALE"))
            .andExpect(jsonPath("$.data.balanceQuantity").value(5))
            .andExpect(jsonPath("$.data.balanceUnitPrice").value(10.0))
            .andExpect(jsonPath("$.data.totalBalance").value(50.0))
            .andExpect(jsonPath("$.data.date").exists());
        }

        @Test
        @DisplayName("Should successfully register a sale with quantity and verify calculations")
        void shouldRegisterSaleWithLargeQuantity() throws Exception {
            // Given: A valid sale request with large quantity
            String requestBody = """
                {
                    "quantity": 5,
                    "factCode": 2,
                    "details": "Sale of 5 units",
                    "idProduct": 1
                }
            """;

            //When/Then: the request is processed successfully
            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("Kardex sale registered successfully"))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.quantity").value(5))
            .andExpect(jsonPath("$.data.unitPrice").value(10.0))
            .andExpect(jsonPath("$.data.details").value("Sale of 5 units"))
            .andExpect(jsonPath("$.data.type").value("SALE"))
            .andExpect(jsonPath("$.data.balanceQuantity").value(5))
            .andExpect(jsonPath("$.data.balanceUnitPrice").value(10.0))
            .andExpect(jsonPath("$.data.totalBalance").value(50.0))
            .andExpect(jsonPath("$.data.date").exists());
        }

        @Test
        @DisplayName("Should successfully register multiple sales and verify weighted average")
        void shouldRegisterMultipleSalesAndVerifyWeightedAverage() throws Exception {
            // Given: First sale
            String firstSale = """
                {
                    "quantity": 5,
                    "factCode": 11111,
                    "details": "First sale",
                    "idProduct": 1
                }
                """;

            // When: First sale is processed
            mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstSale)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balanceQuantity").value(5)) // 10 - 5
                .andExpect(jsonPath("$.data.balanceUnitPrice").value(10.0))
                .andExpect(jsonPath("$.data.totalBalance").value(50.0));


            // Given: Second sale with different price
            String secondSale = """
                {
                    "quantity": 2,
                    "factCode": 22222,
                    "details": "Second sale",
                    "idProduct": 1
                }
                """;

            // When: Second sale is processed
            mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondSale)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balanceQuantity").value(3)) // 5 - 2
                .andExpect(jsonPath("$.data.balanceUnitPrice").value(10.0))
                .andExpect(jsonPath("$.data.totalBalance").value(30.0));
        }

        @Test
        @DisplayName("Should reject request when quantity is missing")
        void shouldRejectWhenQuantityMissing() throws Exception {

            String requestBody = """
                {
                    "factCode": 2,
                    "details": "Missing quantity",
                    "idProduct": 1
                }
            """;

            mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request when factCode is missing")
        void shouldRejectWhenFactCodeMissing() throws Exception {

            String requestBody = """
                {
                    "quantity": 5,
                    "details": "Missing factCode",
                    "idProduct": 1
                }
            """;

            mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request when idProduct is missing")
        void shouldRejectWhenIdProductMissing() throws Exception {

            String requestBody = """
                {
                    "quantity": 5,
                    "factCode": 2,
                    "details": "Missing idProduct"
                }
            """;

            mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle sale with empty details field")
        void shouldHandleEmptyDetailsField() throws Exception {
            String requestBody = """
                {
                    "quantity": 5,
                    "factCode": 2,
                    "idProduct": 1
                }
            """;

            mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.details").value("Venta - Factura: 2"));
        }

        @Test
        @DisplayName("Should reject sale request for non-existent product")
        void shouldRejectWhenProductNotFound() throws Exception {
            String requestBody = """
                {
                    "quantity": 5,
                    "factCode": 2,
                    "idProduct": 9999
                }
            """;

            mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject sale request for non-existent initial stock (Purchase)")
        void shouldRejectWhenInitialStockNotFound() throws Exception {
            // Clear existing kardex entries
            kardexRepository.deleteAll();

            String requestBody = """
                {
                    "quantity": 5,
                    "factCode": 2,
                    "idProduct": 1
                }
            """;
            mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(status().isNotFound());
         }

        @Test
        @DisplayName("Should reject sale request with negative quantity")
        void shouldRejectWhenQuantityIsNegative() throws Exception {
            String requestBody = """
                {
                    "quantity": -5,
                    "factCode": 2,
                    "idProduct": 1
                }
            """;

            mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(status().isBadRequest());
        }

    }

}
