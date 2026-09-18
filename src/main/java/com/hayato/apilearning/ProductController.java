package com.hayato.apilearning;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;


@RestController 
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse>createProduct(
           @Valid @RequestBody ProductRequest request) {
        //TODO: process POST request
        
        ProductResponse response = 
                productService.createProduct(request);

        return ResponseEntity 
                .status(HttpStatus.CREATED)
                .body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(
        @PathVariable long id) {

        return ResponseEntity.ok(
            productService.getProduct(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
        @PathVariable long id,
        @Valid @RequestBody ProductRequest request) {

        return ResponseEntity.ok(
            productService.updateProduct(id, request)
        );
    }
 
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
        @PathVariable long id) {

        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts() {

        return ResponseEntity.ok(
                productService.getProducts()
        );
    }
}
