package product_service.service;

import product_service.dto.request.CategoryCreateVsUpdateRequest;
import product_service.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryCreateVsUpdateRequest request);
    CategoryResponse getCategoryById(Long id);
    CategoryResponse updateCategory(Long id, CategoryCreateVsUpdateRequest request);
    void deleteCategory(Long id);
    List<CategoryResponse> getAllCategories();
}
