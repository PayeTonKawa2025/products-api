package fr.payetonkawa.products.service;

import fr.payetonkawa.products.dto.ProductDto;
import fr.payetonkawa.products.entity.Product;
import fr.payetonkawa.products.exception.MissingDataException;
import fr.payetonkawa.products.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProductService {

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

    public ProductDto updateProduct(Long id, ProductDto updatedProduct) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new MissingDataException("Product not found"));

        if (updatedProduct.getName() == null || updatedProduct.getPrice() <= 0) {
            throw new IllegalArgumentException("Product name and price must be provided");
        }
        product.setName(updatedProduct.getName());
        product.setPrice(updatedProduct.getPrice());
        product.setDescription(updatedProduct.getDescription());
        product.setColor(updatedProduct.getColor());
        product.setStock(updatedProduct.getStock());
        Product savedProduct = productRepository.save(product);
        return ProductDto.fromEntity(savedProduct);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

}
