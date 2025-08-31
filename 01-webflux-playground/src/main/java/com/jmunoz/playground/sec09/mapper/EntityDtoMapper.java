package com.jmunoz.playground.sec09.mapper;

import com.jmunoz.playground.sec09.dto.ProductDto;
import com.jmunoz.playground.sec09.entity.Product;

public class EntityDtoMapper {

    public static Product toEntity(ProductDto dto) {
        var product = new Product();
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setId(dto.id());
        return product;
    }

    public static ProductDto toDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getDescription(),
                product.getPrice()
        );
    }
}
