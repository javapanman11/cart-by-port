package com.hayato.apilearning;

public class ProductNotFoundException extends RuntimeException {
  
      public ProductNotFoundException(long id) {
        super("商品が見つかりません。id =" + id);
      }
}
