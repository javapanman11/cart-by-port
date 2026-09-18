package com.hayato.apilearning;

import java.math.BigDecimal;

public class ProductResponse {

    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String message;

    public ProductResponse(long id, String name, BigDecimal price, Integer stock, String message) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.message = message;
    }

    public long getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public BigDecimal getPrice() {
      return price;
    }

    public Integer getStock() {
      return stock;
    }

    public String getMessage() {
      return message;
    }
}
