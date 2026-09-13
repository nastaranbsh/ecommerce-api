package com.nbsh.commerceapi.category;

import com.nbsh.commerceapi.category.dto.CategoryResponse;
import com.nbsh.commerceapi.category.dto.CreateCategoryRequest;
import com.nbsh.commerceapi.category.dto.UpdateCategoryRequest;
import com.nbsh.commerceapi.common.exception.ResourceConflictException;
import com.nbsh.commerceapi.common.exception.ResourceNotFoundException;
import com.nbsh.commerceapi.product.ProductRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            ProductRepository productRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CategoryResponse createCategory(
            CreateCategoryRequest request
    ) {

        String name = request.name().trim();
        String slug = normalizeSlug(request.slug());

        validateUniqueName(name, null);
        validateUniqueSlug(slug, null);

        Category category = new Category(
                name,
                slug,
                request.description(),
                request.active() == null || request.active()
        );

        Category savedCategory =
                categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {

        return toResponse(findCategory(id));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CategoryResponse updateCategory(
            Long id,
            UpdateCategoryRequest request
    ) {

        Category category = findCategory(id);

        String name = request.name().trim();
        String slug = normalizeSlug(request.slug());

        validateUniqueName(name, id);
        validateUniqueSlug(slug, id);

        category.setName(name);
        category.setSlug(slug);
        category.setDescription(request.description());
        category.setActive(request.active());

        return toResponse(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteCategory(Long id) {

        Category category = findCategory(id);

        if (productRepository.existsByCategoryId(id)) {
            throw new ResourceConflictException(
                    "Category cannot be deleted because products are assigned to it"
            );
        }

        categoryRepository.delete(category);
    }

    private Category findCategory(Long id) {

        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found with id: " + id
                        )
                );
    }

    private void validateUniqueName(
            String name,
            Long currentCategoryId
    ) {

        boolean exists =
                categoryRepository.existsByNameIgnoreCase(name);

        if (!exists) {
            return;
        }

        if (currentCategoryId != null) {
            Category current = findCategory(currentCategoryId);

            if (current.getName().equalsIgnoreCase(name)) {
                return;
            }
        }

        throw new ResourceConflictException(
                "Category name already exists: " + name
        );
    }

    private void validateUniqueSlug(
            String slug,
            Long currentCategoryId
    ) {

        boolean exists =
                categoryRepository.existsBySlugIgnoreCase(slug);

        if (!exists) {
            return;
        }

        if (currentCategoryId != null) {
            Category current = findCategory(currentCategoryId);

            if (current.getSlug().equalsIgnoreCase(slug)) {
                return;
            }
        }

        throw new ResourceConflictException(
                "Category slug already exists: " + slug
        );
    }

    private String normalizeSlug(String slug) {

        return slug
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private CategoryResponse toResponse(
            Category category
    ) {

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}