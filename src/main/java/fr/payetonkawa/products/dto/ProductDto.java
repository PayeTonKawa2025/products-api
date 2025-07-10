package fr.payetonkawa.products.dto;

import fr.payetonkawa.products.entity.Product;
import lombok.Data;

import java.util.List;

@Data
public class ProductDto {

    private Long id;
    private String name;
    private double price;
    private String description;
    private String color;
    private int stock;

    public static ProductDto fromEntity(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        dto.setColor(product.getColor());
        dto.setStock(product.getStock());
        return dto;
    }

    public static List<ProductDto> fromEntities(List<Product> products) {
        return products.stream()
                .map(ProductDto::fromEntity)
                .toList();
    }

}
