package fr.payetonkawa.products.service;

import fr.payetonkawa.products.dto.ProductDto;
import fr.payetonkawa.products.entity.Product;
import fr.payetonkawa.products.event.EventPublisher;
import fr.payetonkawa.products.exception.MissingDataException;
import fr.payetonkawa.products.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProductService {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public List<ProductDto> getAllProducts() {
        return ProductDto.fromEntities(productRepository.findAll());
    }

    public Optional<ProductDto> getProductById(Long id) {
        return productRepository.findById(id)
                .map(ProductDto::fromEntity);
    }

    public ProductDto createProduct(ProductDto product) {
        if (product.getName() == null || product.getPrice() <= 0) {
            throw new IllegalArgumentException("Product name and price must be provided");
        }
        Product newProduct = new Product();

        newProduct.setName(product.getName());
        newProduct.setPrice(product.getPrice());
        newProduct.setDescription(product.getDescription());
        newProduct.setColor(product.getColor());
        newProduct.setStock(product.getStock());

        Product savedProduct = productRepository.save(newProduct);
        return ProductDto.fromEntity(savedProduct);
    }

public ProductDto updateProduct(Long id, ProductDto product) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new MissingDataException("Product not found"));

        if (product.getName() != null) {
            existingProduct.setName(product.getName());
        }
        if (product.getPrice() > 0) {
            existingProduct.setPrice(product.getPrice());
        }
        if (product.getDescription() != null) {
            existingProduct.setDescription(product.getDescription());
        }
        if (product.getColor() != null) {
            existingProduct.setColor(product.getColor());
        }
        if (product.getStock() >= 0) {
            existingProduct.setStock(product.getStock());
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return ProductDto.fromEntity(updatedProduct);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public boolean verifyAndUpdateStock(List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            Long productId = Long.valueOf(item.get("itemId").toString());
            int quantity = (int) item.get("quantity");
            var product = productRepository.findById(productId).orElse(null);
            if (product == null || product.getStock() < quantity) {
                return false;
            }
        }

        for (Map<String, Object> item : items) {
            Long productId = Long.valueOf(item.get("itemId").toString());
            int quantity = (int) item.get("quantity");
            var product = productRepository.findById(productId)
                    .orElseThrow(() -> new MissingDataException("Product not found with id: " + productId));
            product.setStock(product.getStock() - quantity);
            productRepository.save(product);
        }

        return true;
    }

    public void restoreStock(Long productId, int quantity) {
        var product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            log.warn("🚫 Product with id {} not found, cannot restore stock", productId);
            return;
        }

        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
        log.info("🔄 Restored {} units to product ID {}", quantity, productId);
    }



}
