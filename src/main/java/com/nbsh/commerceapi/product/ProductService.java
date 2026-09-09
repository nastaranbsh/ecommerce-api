package com.nbsh.commerceapi.product;

import com.nbsh.commerceapi.product.dto.CreateProductRequest;
import com.nbsh.commerceapi.product.dto.ProductResponse;
import com.nbsh.commerceapi.product.dto.UpdateProductRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {

        if (productRepository.existsBySku(request.sku())) {
            throw new IllegalArgumentException(
                    "Product with SKU already exists: " + request.sku()
            );
        }

        Product product = new Product(
                request.name(),
                request.description(),
                request.price(),
                request.sku(),
                request.active() == null || request.active()
        );

        Product savedProduct = productRepository.save(product);

        return toResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found with id: " + id
                        )
                );

        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts() {

        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse updateProduct(
            Long id,
            UpdateProductRequest request
    ) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found with id: " + id
                        )
                );

        if (!product.getSku().equals(request.sku())
                && productRepository.existsBySku(request.sku())) {

            throw new IllegalArgumentException(
                    "Product with SKU already exists: " + request.sku()
            );
        }

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setSku(request.sku());

        if (request.active() != null) {
            product.setActive(request.active());
        }

        return toResponse(product);
    }

    @Transactional
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found with id: " + id
                        )
                );

        productRepository.delete(product);
    }

    private ProductResponse toResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getSku(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}