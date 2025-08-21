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
import java.util.Map;
import java.util.HashMap;

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
        product.setName("Test");
        product.setPrice(10.0);
        List<Product> products = List.of(product);
        when(productRepository.findAll()).thenReturn(products);

        List<ProductDto> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getName());
        verify(productRepository).findAll();
    }

    @Test
    void testGetProductByIdFound() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test");
        product.setPrice(10.0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<ProductDto> result = productService.getProductById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test", result.get().getName());
    }

    @Test
    void testGetProductByIdNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<ProductDto> result = productService.getProductById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateProductValid() {
        ProductDto dto = ProductDto.builder()
                .name("Test")
                .price(10.0)
                .description("desc")
                .color("red")
                .stock(5)
                .build();

        Product saved = new Product();
        saved.setId(1L);
        saved.setName("Test");
        saved.setPrice(10.0);
        saved.setDescription("desc");
        saved.setColor("red");
        saved.setStock(5);

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDto result = productService.createProduct(dto);

        assertNotNull(result);
        assertEquals("Test", result.getName());
        assertEquals(10.0, result.getPrice());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testCreateProductInvalid() {
        ProductDto dto = ProductDto.builder()
                .name(null)
                .price(0)
                .build();

        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(dto));
    }

    @Test
    void testUpdateProductFound() {
        Product existing = new Product();
        existing.setId(1L);
        existing.setName("Old");
        existing.setPrice(5.0);
        existing.setDescription("old desc");
        existing.setColor("blue");
        existing.setStock(2);

        ProductDto dto = ProductDto.builder()
                .name("New")
                .price(15.0)
                .description("new desc")
                .color("green")
                .stock(10)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(existing);

        ProductDto result = productService.updateProduct(1L, dto);

        assertEquals("New", result.getName());
        assertEquals(15.0, result.getPrice());
        assertEquals("new desc", result.getDescription());
        assertEquals("green", result.getColor());
        assertEquals(10, result.getStock());
    }

    @Test
    void testUpdateProductNotFound() {
        ProductDto dto = ProductDto.builder().name("Test").price(10.0).build();
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(MissingDataException.class, () -> productService.updateProduct(1L, dto));
    }

    @Test
    void testDeleteProduct() {
        doNothing().when(productRepository).deleteById(1L);

        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void testVerifyAndUpdateStockSuccess() {
        Product product = new Product();
        product.setId(1L);
        product.setStock(10);

        Map<String, Object> item = new HashMap<>();
        item.put("itemId", 1L);
        item.put("quantity", 5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        boolean result = productService.verifyAndUpdateStock(List.of(item));

        assertTrue(result);
        verify(productRepository, times(2)).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testVerifyAndUpdateStockFail() {
        Product product = new Product();
        product.setId(1L);
        product.setStock(2);

        Map<String, Object> item = new HashMap<>();
        item.put("itemId", 1L);
        item.put("quantity", 5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        boolean result = productService.verifyAndUpdateStock(List.of(item));

        assertFalse(result);
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testRestoreStockProductFound() {
        Product product = new Product();
        product.setId(1L);
        product.setStock(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.restoreStock(1L, 3);

        assertEquals(8, product.getStock());
        verify(productRepository).save(product);
    }

    @Test
    void testRestoreStockProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        productService.restoreStock(1L, 3);

        verify(productRepository, never()).save(any(Product.class));
    }
}