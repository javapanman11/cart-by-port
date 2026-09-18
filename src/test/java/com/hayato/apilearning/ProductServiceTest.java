package com.hayato.apilearning;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void 商品を登録できる() {

        ProductRequest request = new ProductRequest();
        request.setName("キーボード");
        request.setPrice(new BigDecimal("5000"));
        request.setStock(10);

        Product savedProduct = new Product(
                "キーボード",
                new BigDecimal("5000"),
                10
        );

        ReflectionTestUtils.setField(savedProduct, "id", 1L);

        given(productRepository.save(any(Product.class)))
                .willReturn(savedProduct);

        ProductResponse response =
                productService.createProduct(request);

        assertEquals(1L, response.getId());
        assertEquals("キーボード", response.getName());
        assertEquals(new BigDecimal("5000"), response.getPrice());
        assertEquals(10, response.getStock());
        assertEquals("商品を登録しました", response.getMessage());
    }

    @Test
    void 商品を1件取得できる() {

        Product product = new Product(
                "マウス",
                new BigDecimal("3000"),
                5
        );

        ReflectionTestUtils.setField(product, "id", 1L);

        given(productRepository.findById(1L))
                .willReturn(Optional.of(product));

        ProductResponse response =
                productService.getProduct(1L);

        assertEquals(1L, response.getId());
        assertEquals("マウス", response.getName());
        assertEquals(new BigDecimal("3000"), response.getPrice());
        assertEquals(5, response.getStock());
        assertEquals("商品を取得しました", response.getMessage());
    }

    @Test
    void 存在しない商品を取得すると例外になる() {

        given(productRepository.findById(999L))
                .willReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProduct(999L)
        );
    }

    @Test
    void 商品を更新できる() {

        Product product = new Product(
                "旧マウス",
                new BigDecimal("2000"),
                3
        );

        ReflectionTestUtils.setField(product, "id", 1L);

        given(productRepository.findById(1L))
                .willReturn(Optional.of(product));

        ProductRequest request = new ProductRequest();
        request.setName("新マウス");
        request.setPrice(new BigDecimal("3000"));
        request.setStock(5);

        ProductResponse response =
                productService.updateProduct(1L, request);

        assertEquals(1L, response.getId());
        assertEquals("新マウス", response.getName());
        assertEquals(new BigDecimal("3000"), response.getPrice());
        assertEquals(5, response.getStock());
        assertEquals("商品を更新しました。", response.getMessage());
    }

    @Test
    void 商品を削除できる() {

        Product product = new Product(
                "マウス",
                new BigDecimal("3000"),
                5
        );

        ReflectionTestUtils.setField(product, "id", 1L);

        given(productRepository.findById(1L))
                .willReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository).delete(product);
    }
}