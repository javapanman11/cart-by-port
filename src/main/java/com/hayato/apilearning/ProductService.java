package com.hayato.apilearning;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse createProduct(ProductRequest request) {

    // ① Entityを作る
        Product product = new Product(
            request.getName(),
            request.getPrice(),
            request.getStock()
        );

        Product savedProduct = productRepository.save(product);

        return new ProductResponse(
            savedProduct.getId(),
            savedProduct.getName(),
            savedProduct.getPrice(),
            savedProduct.getStock(),
            "商品を登録しました"
        );
    }

    public List<ProductResponse> getProducts() {
        
      List<Product> products = productRepository.findAll();

      List<ProductResponse> responses = new ArrayList<>();

      for(Product product : products) {

          responses.add(
                  new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.getStock(),
                        "商品を取得しました"
                  )
          );
      }

      return responses;
    }

    public ProductResponse getProduct(long id) {

          Product product = productRepository.findById(id)
              .orElseThrow(() -> new ProductNotFoundException(id));

          return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getStock(),
            "商品を取得しました"
          );
    }

    @Transactional 
    public ProductResponse updateProduct(
      long id,
      ProductRequest request) {

      Product product = productRepository.findById(id)
          .orElseThrow(() -> new ProductNotFoundException(id));
      
      product.setName(request.getName());
      product.setPrice(request.getPrice());
      product.setStock(request.getStock());

      return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getPrice(),
        product.getStock(),
        "商品を更新しました。"
      );
    }

    public void deleteProduct(long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

        productRepository.delete(product);
    }
}