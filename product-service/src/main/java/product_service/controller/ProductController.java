package product_service.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import product_service.config.RedisConfig;
import product_service.dto.request.ProductCreationRequest;
import product_service.dto.request.ProductUpdateRequest;
import product_service.dto.response.ApiResponse;
import product_service.dto.response.ProductResponse;
import product_service.dto.response.ProductSummaryResponse;
import product_service.process.TaskManager;
import product_service.service.ProductService;
import product_service.service.RedisCacheService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductService productService;
    TaskManager taskManager;

    @PostMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> createProduct(@RequestBody ProductCreationRequest request) {
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully created product");
        apiResponse.setResult(productService.createProduct(request));
        return apiResponse;
    }

    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateRequest request) {
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully updated product");
        apiResponse.setResult(productService.updateProduct(id, request));
        return apiResponse;
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully deleted product");
        productService.deleteProduct(id);
        return apiResponse;
    }

    //    @GetMapping
//    public ApiResponse<List<ProductResponse>> getAllProducts() {
//        ApiResponse<List<ProductResponse>> apiResponse = new ApiResponse<>();
//        apiResponse.setMessage("Successfully retrieved products");
//        apiResponse.setResult(productService.getAllProducts());
//        return apiResponse;
//    }
    @GetMapping
    public ApiResponse<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Lấy page từ repository qua service
        Page<ProductResponse> products = productService.getAllProducts(pageable);

        ApiResponse<Page<ProductResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully retrieved products");
        apiResponse.setResult(products);
        return apiResponse;
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long id) {
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>();
        taskManager.execute(productService, id);
//        productService.getRelatedProductSummaryByProductId(id);
        apiResponse.setMessage("Successfully retrieved product");
        apiResponse.setResult(productService.getProduct(id));
        return apiResponse;
    }

    @GetMapping("/{id}/relatedProduct")
    public ApiResponse<List<ProductSummaryResponse>> getRelatedProductsSummary(@PathVariable Long id) {
        ApiResponse<List<ProductSummaryResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully retrieved product");
        apiResponse.setResult(productService.getRelatedProductSummaryByProductId(id));
        return apiResponse;
    }


    @GetMapping("/jdbc/{id}")
    public ApiResponse<Map<String, Object>> getProductNamedJDBC(@PathVariable Long id) {
        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully retrieved product");
        apiResponse.setResult(productService.getProductByIdNamedJDBC(id));
        return apiResponse;
    }

    @GetMapping("/category/{id}")
    public ApiResponse<List<ProductResponse>> getProductsByCategory(@PathVariable Long id) {
        ApiResponse<List<ProductResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully retrieved products");
        apiResponse.setResult(productService.getAllProductsByCategory(id));
        return apiResponse;
    }

    // dùng store procedure + named jdbc
    @GetMapping("/category/{id}/by-price-desc")
    public ApiResponse<List<Map<String, Object>>> getProductsByCategoryAndSortByPrice(@PathVariable Long id) {
        ApiResponse<List<Map<String, Object>>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully retrieved products by category");
        apiResponse.setResult(productService.getProductsByCategoryOrderByPriceDesc(id));
        return apiResponse;
    }

    @GetMapping("/author/{author}")
    public ApiResponse<List<Map<String, Object>>> getProductsByAuthor(@PathVariable String author) {
        ApiResponse<List<Map<String, Object>>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully retrieved products by author");
        apiResponse.setResult(productService.getProductsByAuthor(author));
        return apiResponse;
    }

    @GetMapping("/spec/search")
    public ApiResponse<Page<ProductResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ApiResponse<Page<ProductResponse>> response = new ApiResponse<>();
        response.setMessage("Successfully retrieved products");
        response.setResult(productService.fullSearch(name, author, language,
                categoryIds, minPrice, maxPrice, PageRequest.of(page, size)));
        return response;
    }

    @GetMapping("/top-sold")
    public ApiResponse<List<Map<String, Object>>> getTopSoldProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        ApiResponse<List<Map<String, Object>>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully retrieved top-sold products");
        apiResponse.setResult(productService.getTopSoldProducts(startDate, endDate));
        return apiResponse;
    }

    @GetMapping("/top-bought/supplier/{id}")
    public ApiResponse<List<Map<String, Object>>> getTopBoughtSupplierProducts(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        ApiResponse<List<Map<String, Object>>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully retrieved top-bought supplier products");
        apiResponse.setResult(productService.getPurchaseOrderItemBySupplierId(id, startDate, endDate));
        return apiResponse;
    }

}
