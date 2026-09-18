package com.hayato.apilearning;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class ProductRequest {
  
    @NotBlank(message = "商品名は必須です")
    private String name;

    @NotNull(message = "金額は必須です")
    @PositiveOrZero(message = "金額は0円以上を指定してください")
    private BigDecimal price;

    @NotNull(message = "在庫は必須です")
    @PositiveOrZero(message = "在庫は0以上を指定してください")
    private Integer stock;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
