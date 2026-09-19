package com.hayato.apilearning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

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

    @Autowired
    private JsonMapper jsonMapper;

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

        assertEquals(
                "E2Eキーボード",
                savedProduct.getName()
        );

        assertEquals(
                4,
                savedProduct.getStock()
        );

        assertTrue(
                savedProduct.getPrice()
                        .compareTo(new BigDecimal("7000")) == 0
        );
    }

    @Test
    void 登録した商品をAPIで取得できる() throws Exception {

        MvcResult result = mockMvc.perform(
                post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "APIマウス",
                                  "price": 3500,
                                  "stock": 7
                                }
                                """)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("APIマウス"))
        .andExpect(jsonPath("$.price").value(3500))
        .andExpect(jsonPath("$.stock").value(7))
        .andExpect(jsonPath("$.message").value("商品を登録しました"))
        .andReturn();

        String responseBody =
                result.getResponse().getContentAsString();

        JsonNode json =
                jsonMapper.readTree(responseBody);

        long productId =
                json.get("id").asLong();

        mockMvc.perform(
                get("/products/{id}", productId)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(productId))
        .andExpect(jsonPath("$.name").value("APIマウス"))
        .andExpect(jsonPath("$.price").value(3500))
        .andExpect(jsonPath("$.stock").value(7))
        .andExpect(jsonPath("$.message").value("商品を取得しました"));
    }

    @Test
    void 登録した商品をAPIで更新できる() throws Exception {

        // ① 商品を登録
        MvcResult result = mockMvc.perform(
                post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "更新前マウス",
                                  "price": 3000,
                                  "stock": 5
                                }
                                """)
        )
        .andExpect(status().isCreated())
        .andReturn();

        // ② POSTレスポンスからidを取得
        String responseBody =
                result.getResponse().getContentAsString();

        JsonNode json =
                jsonMapper.readTree(responseBody);

        long productId =
                json.get("id").asLong();

        // ③ PUTで商品を更新
        mockMvc.perform(
                put("/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "更新後マウス",
                                  "price": 4500,
                                  "stock": 8
                                }
                                """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(productId))
        .andExpect(jsonPath("$.name").value("更新後マウス"))
        .andExpect(jsonPath("$.price").value(4500))
        .andExpect(jsonPath("$.stock").value(8))
        .andExpect(jsonPath("$.message").value("商品を更新しました。"));

        // ④ DBへ確実に反映
        entityManager.flush();
        entityManager.clear();

        // ⑤ PostgreSQLから再取得
        Product updatedProduct =
                productRepository.findById(productId)
                        .orElseThrow();

        // ⑥ DBの値も更新されているか確認
        assertEquals(
                "更新後マウス",
                updatedProduct.getName()
        );

        assertEquals(
                8,
                updatedProduct.getStock()
        );

        assertTrue(
                updatedProduct.getPrice()
                        .compareTo(new BigDecimal("4500")) == 0
        );
    }

    @Test
    void 登録した商品をAPIで削除できる() throws Exception {

        // ① 商品を登録
        MvcResult result = mockMvc.perform(
                post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "削除対象キーボード",
                                  "price": 6000,
                                  "stock": 3
                                }
                                """)
        )
        .andExpect(status().isCreated())
        .andReturn();

        // ② POSTレスポンスからidを取得
        String responseBody =
                result.getResponse().getContentAsString();

        JsonNode json =
                jsonMapper.readTree(responseBody);

        long productId =
                json.get("id").asLong();

        // ③ DELETE APIを実行
        mockMvc.perform(
                delete("/products/{id}", productId)
        )
        .andExpect(status().isNoContent());

        // ④ DBへ確実に反映
        entityManager.flush();
        entityManager.clear();

        // ⑤ DBに存在しないことを確認
        boolean exists =
                productRepository.existsById(productId);

        assertEquals(false, exists);
    }
}