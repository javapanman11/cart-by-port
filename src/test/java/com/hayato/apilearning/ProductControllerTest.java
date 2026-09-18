package com.hayato.apilearning;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void 商品を登録すると201が返る() throws Exception {

        ProductResponse response = new ProductResponse(
                1L,
                "キーボード",
                new BigDecimal("5000"),
                10,
                "商品を登録しました"
        );

        given(productService.createProduct(any(ProductRequest.class)))
                .willReturn(response);

        mockMvc.perform(
                post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "キーボード",
                                  "price": 5000,
                                  "stock": 10
                                }
                                """)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("キーボード"))
        .andExpect(jsonPath("$.price").value(5000))
        .andExpect(jsonPath("$.stock").value(10))
        .andExpect(jsonPath("$.message").value("商品を登録しました"));
    }

    @Test
    void 商品名が空の場合は400が返る() throws Exception {

        mockMvc.perform(
                post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "name": "",
                                "price": 5000,
                                "stock": 10
                                }
                                """)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message")
                .value("入力内容に誤りがあります"))
        .andExpect(jsonPath("$.errors.name")
            .value("商品名は必須です"));
    }
}