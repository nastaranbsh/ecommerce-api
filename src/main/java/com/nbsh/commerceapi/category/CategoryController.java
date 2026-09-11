package com.nbsh.commerceapi.category;

import com.nbsh.commerceapi.category.dto.CategoryResponse;
import com.nbsh.commerceapi.category.dto.CreateCategoryRequest;
import com.nbsh.commerceapi.category.dto.UpdateCategoryRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(
            CategoryService categoryService
    ) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {

        CategoryResponse category =
                categoryService.createCategory(request);

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/v1/categories/" + category.id()
                        )
                )
                .body(category);
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategory(
            @PathVariable Long id
    ) {
        return categoryService.getCategory(id);
    }

    @GetMapping
    public List<CategoryResponse> getCategories() {

        return categoryService.getCategories();
    }

    @PutMapping("/{id}")
    public CategoryResponse updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {

        return categoryService.updateCategory(
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id
    ) {

        categoryService.deleteCategory(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}