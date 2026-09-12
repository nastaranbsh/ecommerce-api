package com.nbsh.commerceapi.product;

import com.nbsh.commerceapi.category.Category;
import com.nbsh.commerceapi.category.CategoryRepository;
import com.nbsh.commerceapi.common.api.PageResponse;
import com.nbsh.commerceapi.common.exception.InvalidRequestException;
import com.nbsh.commerceapi.common.exception.ResourceConflictException;
import com.nbsh.commerceapi.common.exception.ResourceNotFoundException;
import com.nbsh.commerceapi.product.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ProductResponse createProduct(
            CreateProductRequest request
    ) {

        if (productRepository.existsBySku(request.sku())) {
            throw new ResourceConflictException(
                    "Product with SKU already exists: "
                            + request.sku()
            );
        }

        Category category =
                findCategory(request.categoryId());

        Product product = new Product(
                request.name(),
                request.description(),
                request.price(),
                request.sku(),
                request.active() == null || request.active(),
                category
        );

        Product savedProduct =
                productRepository.save(product);

        return toResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {

        return toResponse(findProduct(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(
            ProductFilter filter,
            Pageable pageable
    ) {

        validatePriceRange(filter);

        Specification<Product> specification =
                ProductSpecification.withFilters(filter);

        Page<Product> productPage =
                productRepository.findAll(
                        specification,
                        pageable
                );

        List<ProductResponse> content =
                productPage.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return new PageResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isFirst(),
                productPage.isLast()
        );
    }

    @Transactional
    public ProductResponse updateProduct(
            Long id,
            UpdateProductRequest request
    ) {

        Product product = findProduct(id);

        if (!product.getSku().equals(request.sku())
                && productRepository.existsBySku(request.sku())) {

            throw new ResourceConflictException(
                    "Product with SKU already exists: "
                            + request.sku()
            );
        }

        Category category =
                findCategory(request.categoryId());

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setSku(request.sku());
        product.setActive(request.active());
        product.setCategory(category);

        return toResponse(product);
    }

    @Transactional
    public void deleteProduct(Long id) {

        Product product = findProduct(id);

        productRepository.delete(product);
    }

    private Product findProduct(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + id
                        )
                );
    }

    private Category findCategory(Long id) {

        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found with id: " + id
                        )
                );
    }

    private void validatePriceRange(
            ProductFilter filter
    ) {

        if (filter.minPrice() != null
                && filter.minPrice().signum() < 0) {

            throw new InvalidRequestException(
                    "Minimum price cannot be negative"
            );
        }

        if (filter.maxPrice() != null
                && filter.maxPrice().signum() < 0) {

            throw new InvalidRequestException(
                    "Maximum price cannot be negative"
            );
        }

        if (filter.minPrice() != null
                && filter.maxPrice() != null
                && filter.minPrice()
                .compareTo(filter.maxPrice()) > 0) {

            throw new InvalidRequestException(
                    "Minimum price cannot be greater than maximum price"
            );
        }
    }

    private ProductResponse toResponse(Product product) {

        Category category = product.getCategory();

        CategorySummaryResponse categoryResponse =
                new CategorySummaryResponse(
                        category.getId(),
                        category.getName(),
                        category.getSlug()
                );

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getSku(),
                product.isActive(),
                categoryResponse,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}