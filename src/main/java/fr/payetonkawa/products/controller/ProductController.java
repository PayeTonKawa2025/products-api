package fr.payetonkawa.products.controller;

import fr.payetonkawa.products.dto.ProductDto;
import fr.payetonkawa.products.entity.Product;
import fr.payetonkawa.products.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
@AllArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductDto> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ProductDto createProduct(@RequestBody ProductDto product) {
        return productService.createProduct(product);
    }

    @PatchMapping("/{id}")
    public ProductDto updateProduct(@PathVariable Long id, @RequestBody ProductDto product) {
        return productService.updateProduct(id, product);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

}
