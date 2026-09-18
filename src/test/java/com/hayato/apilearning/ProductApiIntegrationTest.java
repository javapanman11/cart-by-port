package com.hayato.apilearning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 商品登録APIでデータベースに商品が保存される() throws Exception {

        mockMvc.perform(
                post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "E2Eキーボード",
                                  "price": 7000,
                                  "stock": 4
                                }
                                """)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("E2Eキーボード"))
        .andExpect(jsonPath("$.price").value(7000))
        .andExpect(jsonPath("$.stock").value(4))
        .andExpect(jsonPath("$.message").value("商品を登録しました"));

        entityManager.flush();
        entityManager.clear();

        List<Product> products = productRepository.findAll();

        Product savedProduct = products.stream()
                .filter(product ->
                        product.getName().equals("E2Eキーボード"))
                .findFirst()
                .orElseThrow();

        assertEquals("E2Eキーボード", savedProduct.getName());
        assertEquals(4, savedProduct.getStock());

        assertTrue(
                savedProduct.getPrice()
                        .compareTo(new java.math.BigDecimal("7000")) == 0
        );
    }
}