package com.paye_ton_kawa.product.service;

import com.paye_ton_kawa.product.entity.Product;
import com.paye_ton_kawa.product.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProductService {
private final ProductRepository productRepository;


    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id)
                .map(p -> {
                    p.setName(updatedProduct.getName());
                    p.setPrice(updatedProduct.getPrice());
                    p.setColor(updatedProduct.getColor());
                    p.setDescription(updatedProduct.getDescription());
                    p.setStock(updatedProduct.getStock());
                    return productRepository.save(p);
                })
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id : " + id));
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}