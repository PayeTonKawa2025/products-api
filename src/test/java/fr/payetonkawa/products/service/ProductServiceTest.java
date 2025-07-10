package fr.payetonkawa.products.service;

import fr.payetonkawa.products.dto.ProductDto;
import fr.payetonkawa.products.entity.Product;
import fr.payetonkawa.products.exception.MissingDataException;
import fr.payetonkawa.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllProducts() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(10.0);

        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductDto> products = productService.getAllProducts();

        assertNotNull(products);
        assertEquals(1, products.size());
        assertEquals("Test Product", products.get(0).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetProductById() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(10.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<ProductDto> productDto = productService.getProductById(1L);

        assertTrue(productDto.isPresent());
        assertEquals("Test Product", productDto.get().getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(10.0);

        ProductDto productDto = new ProductDto();
        productDto.setName("Test Product");
        productDto.setPrice(10.0);

        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDto createdProduct = productService.createProduct(productDto);

        assertNotNull(createdProduct);
        assertEquals("Test Product", createdProduct.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Old Product");
        product.setPrice(5.0);

        ProductDto updatedProductDto = new ProductDto();
        updatedProductDto.setName("Updated Product");
        updatedProductDto.setPrice(15.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDto updatedProduct = productService.updateProduct(1L, updatedProductDto);

        assertNotNull(updatedProduct);
        assertEquals("Updated Product", updatedProduct.getName());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testDeleteProduct() {
        doNothing().when(productRepository).deleteById(1L);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void testCreateProductInvalidData() {
        ProductDto productDto = new ProductDto();
        productDto.setName(null);
        productDto.setPrice(0);

        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(productDto));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testUpdateProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ProductDto updatedProductDto = new ProductDto();
        updatedProductDto.setName("Updated Product");
        updatedProductDto.setPrice(15.0);

        assertThrows(MissingDataException.class, () -> productService.updateProduct(1L, updatedProductDto));
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }
}