package product_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import product_service.dto.request.ProductCreationRequest;
import product_service.dto.request.ProductUpdateRequest;
import product_service.dto.response.ProductResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ProductService {
    ProductResponse createProduct(ProductCreationRequest request);
    ProductResponse updateProduct(Long id, ProductUpdateRequest request);
    ProductResponse getProduct(Long id);
//    List<ProductResponse> getAllProducts();
    Page<ProductResponse> getAllProducts(Pageable pageable);
    List<ProductResponse> getAllProductsByCategory(Long categoryId);
    void deleteProduct(Long id);
    Map<String, Object> getProductByIdNamedJDBC(Long id);
    List<Map<String, Object>> getProductsByCategoryOrderByPriceDesc(Long categoryId);
    List<Map<String, Object>> getProductsByAuthor(String author);
    Page<ProductResponse> fullSearch(String name, String author, String language, List<Long> categoryIds
            , BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    List<Map<String, Object>> getTopSoldProducts(LocalDate startDate, LocalDate endDate);
    List<Map<String, Object>> getPurchaseOrderItemBySupplierId(Long supplierId, LocalDate startDate, LocalDate endDate);
}
