package fr.payetonkawa.products.dto;

import fr.payetonkawa.products.entity.Product;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductDtoTest {

    @Test
    void testFromEntity() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(10.0);
        product.setDescription("Test Description");
        product.setColor("Red");
        product.setStock(100);

        ProductDto productDto = ProductDto.fromEntity(product);

        assertNotNull(productDto);
        assertEquals(1L, productDto.getId());
        assertEquals("Test Product", productDto.getName());
        assertEquals(10.0, productDto.getPrice());
        assertEquals("Test Description", productDto.getDescription());
        assertEquals("Red", productDto.getColor());
        assertEquals(100, productDto.getStock());
    }

    @Test
    void testFromEntities() {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Product 1");
        product1.setPrice(10.0);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setPrice(20.0);

        List<Product> products = List.of(product1, product2);
        List<ProductDto> productDtos = ProductDto.fromEntities(products);

        assertNotNull(productDtos);
        assertEquals(2, productDtos.size());
        assertEquals("Product 1", productDtos.get(0).getName());
        assertEquals("Product 2", productDtos.get(1).getName());
    }
}