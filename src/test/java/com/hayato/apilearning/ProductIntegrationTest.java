package com.hayato.apilearning;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class ProductIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 商品更新がデータベースに反映される() {

        Product product = new Product(
                "旧キーボード",
                new BigDecimal("5000"),
                10
        );

        Product savedProduct = productRepository.save(product);

        ProductRequest request = new ProductRequest();
        request.setName("新キーボード");
        request.setPrice(new BigDecimal("6000"));
        request.setStock(8);

        productService.updateProduct(savedProduct.getId(), request);

        entityManager.flush();
        entityManager.clear();

        Product updatedProduct = productRepository
                .findById(savedProduct.getId())
                .orElseThrow();

        assertEquals("新キーボード", updatedProduct.getName());

        assertEquals(
                0,
                new BigDecimal("6000").compareTo(updatedProduct.getPrice())
        );

        assertEquals(8, updatedProduct.getStock());
    }

    @Test
    void 商品削除がデータベースに反映される() {

        Product product = new Product(
                "削除対象マウス",
                new BigDecimal("3000"),
                5
        );

        Product savedProduct = productRepository.save(product);

        productService.deleteProduct(savedProduct.getId());

        entityManager.flush();
        entityManager.clear();

        boolean exists =
                productRepository.existsById(savedProduct.getId());

        assertEquals(false, exists);
    }
}