package com.nbsh.commerceapi.product;

import com.nbsh.commerceapi.common.api.PageResponse;
import com.nbsh.commerceapi.common.exception.ApiErrorResponse;
import com.nbsh.commerceapi.common.exception.InvalidRequestException;
import com.nbsh.commerceapi.config.OpenApiConfig;
import com.nbsh.commerceapi.product.dto.CreateProductRequest;
import com.nbsh.commerceapi.product.dto.ProductFilter;
import com.nbsh.commerceapi.product.dto.ProductResponse;
import com.nbsh.commerceapi.product.dto.UpdateProductRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/products")
@Tag(
        name = "Products",
        description = "Public product catalog and administrative product management"
)
public class ProductController {

    private final ProductService productService;

    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of(
                    "id",
                    "name",
                    "price",
                    "createdAt",
                    "updatedAt"
            );

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @SecurityRequirement(
            name = OpenApiConfig.BEARER_AUTH
    )
    @Operation(
            summary = "Create product",
            description = "ADMIN only."
    )
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {

        ProductResponse product =
                productService.createProduct(request);

        return ResponseEntity
                .created(URI.create("/api/v1/products/" + product.id()))
                .body(product);
    }

    @Operation(
            summary = "Get product"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product does not exist",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ProductResponse getProduct(
            @PathVariable Long id
    ) {
        return productService.getProduct(id);
    }

    @Operation(
            summary = "Search products",
            description = """
                Returns a paginated product catalog.

                Supports free-text search, category filtering,
                price range filtering, active-state filtering,
                sorting, and pagination.
                """
    )
    @GetMapping
    public PageResponse<ProductResponse> getProducts(
            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            Long categoryId,

            @RequestParam(required = false)
            BigDecimal minPrice,

            @RequestParam(required = false)
            BigDecimal maxPrice,

            @RequestParam(required = false)
            Boolean active,

            @Parameter(
                    description = "Zero-based page index",
                    example = "0"
            )
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(
                    description = "Number of results per page. Maximum 100.",
                    example = "20"
            )
            @RequestParam(defaultValue = "20")
            int size,

            @Parameter(
                    description = "Sort property",
                    example = "price"
            )
            @RequestParam(defaultValue = "id")
            String sortBy,

            @Parameter(
                    description = "Sort direction: asc or desc",
                    example = "asc"
            )
            @RequestParam(defaultValue = "asc")
            String sortDirection
    ) {
        validatePagination(page, size);

        ProductFilter filter = new ProductFilter(
                search,
                categoryId,
                minPrice,
                maxPrice,
                active
        );

        Sort sort = createSort(
                sortBy,
                sortDirection
        );


        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sort
                );

        return productService.getProducts(
                filter,
                pageable
        );
    }

    @SecurityRequirement(
            name = OpenApiConfig.BEARER_AUTH
    )
    @Operation(
            summary = "Update product",
            description = "ADMIN only."
    )
    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return productService.updateProduct(id, request);
    }

    @SecurityRequirement(
            name = OpenApiConfig.BEARER_AUTH
    )
    @Operation(
            summary = "Delete product",
            description = "ADMIN only."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id
    ) {

        productService.deleteProduct(id);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }


    private Sort createSort(
            String sortBy,
            String sortDirection
    ) {

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new InvalidRequestException(
                    "Unsupported sort field: " + sortBy
            );
        }

        Sort.Direction direction;

        if ("asc".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else if ("desc".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new InvalidRequestException(
                    "Sort direction must be 'asc' or 'desc'"
            );
        }

        Sort requestedSort =
                Sort.by(direction, sortBy);

        if ("id".equals(sortBy)) {
            return requestedSort;
        }

        return requestedSort.and(
                Sort.by(Sort.Direction.ASC, "id")
        );
    }

    private void validatePagination(
            int page,
            int size
    ) {

        if (page < 0) {
            throw new InvalidRequestException(
                    "Page number must be greater than or equal to 0"
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidRequestException(
                    "Page size must be between 1 and "
                            + MAX_PAGE_SIZE
            );
        }
    }
}

