package product_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import product_service.dto.request.ProductCreationRequest;
import product_service.dto.request.ProductUpdateRequest;
import product_service.dto.response.ProductResponse;
import product_service.dto.response.ProductResponseForAdmin;
import product_service.dto.response.ProductSummaryResponse;
import product_service.entity.Category;
import product_service.entity.Product;
import product_service.exception.AppException;
import product_service.exception.ErrorCode;
import product_service.mapper.ProductMapper;
import product_service.repo.CategoryRepository;
import product_service.repo.ProductJDBCRepository;
import product_service.repo.ProductRepository;
import product_service.service.ProductService;
import product_service.service.RedisCacheService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static product_service.specification.ProductSpecification.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {
    ProductRepository productRepository;
    ProductJDBCRepository productJDBCRepository;
    ProductMapper productMapper;
    CategoryRepository categoryRepository;
    RedisCacheService redisCacheService;
    ObjectMapper objectMapper;
    ExecutorService executor = Executors.newFixedThreadPool(10);

    @Override
    public ProductResponse getProduct(Long id) {
        String key = "product:" + id;

        // 1. Lấy từ cache
        Object cached = redisCacheService.getValue(key);
        if (cached != null) {
            // Redis lưu JSON -> convert về DTO
            ProductResponse response = objectMapper.convertValue(cached, ProductResponse.class);
            System.out.println("Cache hit for product " + id);
            return response;
        }

//        System.out.println("Cache miss. Querying DB for product " + id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        ProductResponse response = productMapper.toProductResponse(product);
        redisCacheService.setValueWithTimeout(key, response, 20, TimeUnit.SECONDS);
        return response;
    }

    @Override
    public ProductResponseForAdmin getProductByAdmin(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        ProductResponseForAdmin response = productMapper.toProductResponseForAdmin(product);
        return response;
    }

    @Override
    public List<ProductSummaryResponse> getRelatedProductSummaryByProductId(Long id) {
        List<Long> relatedIds = getRelatedProductIdsByProductId(id);
        List<ProductSummaryResponse> relatedProducts = new ArrayList<>();

        // Danh sách Future (đại diện cho các tác vụ chạy song song)
        List<Future<ProductSummaryResponse>> futures = new ArrayList<>();

        for (Long relatedId : relatedIds) {
            futures.add(executor.submit(() -> {
                String summaryKey = "product_summary:" + relatedId;

                ProductSummaryResponse cachedSummary =
                        (ProductSummaryResponse) redisCacheService.getValue(summaryKey);
                if (cachedSummary != null) {
                    return cachedSummary;
                }

//                try {
//                    Thread.sleep(10000);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }

                ProductSummaryResponse summary = productRepository.getProductSummaryById(relatedId);
                if (summary != null) {
                    redisCacheService.setValueWithTimeout(summaryKey, summary, 600, TimeUnit.SECONDS);
                }
                return summary;
            }));
        }

        // ✅ Chờ tất cả task hoàn thành
        for (Future<ProductSummaryResponse> future : futures) {
            try {
                ProductSummaryResponse result = future.get(); // blocking cho từng task
                if (result != null) {
                    relatedProducts.add(result);
                }
            } catch (Exception e) {
                System.err.println("Error fetching related product: " + e.getMessage());
            }
        }

        return relatedProducts;
    }


    @Override
    public List<Long> getRelatedProductIdsByProductId(Long id) {
        String cacheKey = "related_product:" + id;

        // ---------------- Lấy từ Redis ----------------
        List<Object> cachedList = redisCacheService.getList(cacheKey);
        if (cachedList != null && !cachedList.isEmpty()) {
            return cachedList.stream()
                    .filter(Objects::nonNull)
                    .map(o -> Long.parseLong(o.toString()))  // parse String sang Long
                    .toList();
        }

        // ---------------- Nếu không có trong cache -> load từ DB ----------------
        // Lấy trực tiếp category IDs từ DB, tránh lazy-loading
        List<Long> categoryIds = productRepository.findCategoryIdsByProductId(id);
        if (categoryIds.isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        List<Long> relatedProducts = productRepository.findRandomByCategoryListGetIdOnly(categoryIds, id);

        // ---------------- Push vào Redis list ----------------
        List<String> strList = relatedProducts.stream()
                .map(String::valueOf)
                .toList();

        redisCacheService.lPushAll(cacheKey, strList);
        redisCacheService.setTimout(cacheKey, 600, TimeUnit.SECONDS);

        return relatedProducts;
    }


    @Override
    public ProductSummaryResponse getProductProjection(Long id) {
        return productRepository.getProductSummaryById(id);
    }


    @Override
    public ProductResponse createProduct(ProductCreationRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.PRODUCT_EXISTED);
        }
        // Chuyển request thành Product entity (không set categories trước)
        Product product = productMapper.toCreateProduct(request);
        product.setCreatedAt(LocalDateTime.now());
        product.setCost(BigDecimal.ZERO);

        // Lấy danh sách Category entity từ categoryIds
        Set<Category> categoryEntities = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));

        // Gán category entity cho product
        product.setCategories(categoryEntities);

        // Lưu product, Hibernate sẽ tự động cập nhật bảng product_categories
        product = productRepository.save(product);

        return productMapper.toProductResponse(product);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // Kiểm tra tên mới
        if (request.getName() != null && !product.getName().equals(request.getName())
                && productRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.PRODUCT_EXISTED);
        }

        // Cập nhật product bằng mapper
        productMapper.toUpdateProduct(product, request);

        product = productRepository.save(product);
        ProductResponse productResponse = productMapper.toProductResponse(product);

        // Cập nhật cache ngay
        String key = "product:" + id;
        redisCacheService.setValueWithTimeout(key, productResponse, 600, TimeUnit.SECONDS);

        return productResponse;
    }


    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(productMapper::toProductResponse);
    }

    @Override
    @Transactional(readOnly = true)  // ← cần để giữ connection mở, dùng với khai báo StoreProcedure
    public List<ProductResponse> getAllProductsByCategory(Long categoryId) {
        return productRepository.GetProductsByCategoryId(categoryId).stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)  // ← cần để giữ connection mở, dùng với khai báo StoreProcedure
    public List<ProductResponseForAdmin> getAllProductsByCategoryByAdmin(Long categoryId) {
        return productRepository.GetProductsByCategoryId(categoryId).stream()
                .map(productMapper::toProductResponseForAdmin)
                .toList();
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        productRepository.delete(product);
    }

    @Override
    public Map<String, Object> getProductByIdNamedJDBC(Long id) {
        List<Map<String, Object>> rows = productJDBCRepository.findProductWithCategoriesById(id);

        if (rows.isEmpty()) {
            return null; // hoặc throw new AppException(...)
        }

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> firstRow = rows.get(0);
        result.put("id", firstRow.get("product_id"));
        result.put("name", firstRow.get("product_name"));
        result.put("price", firstRow.get("price"));
        result.put("cost", firstRow.get("cost"));

        // Gom danh sách category
        List<String> categories = rows.stream()
                .map(r -> (String) r.get("category_name"))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        result.put("categories", categories);

        return result;
    }

    @Override
    public List<Map<String, Object>> getProductsByCategoryOrderByPriceDesc(Long categoryId) {
        return productJDBCRepository.findProductsByCategoryOrderByPriceDesc(categoryId);
    }

    @Override
    public List<Map<String, Object>> getProductsByAuthor(String author) {
        List<Object[]> rows = productRepository.findProductsByAuthor(author);

        // Map<ProductId, ProductInfo + Set<CategoryName>>
        Map<Long, Map<String, Object>> productMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            Long productId = ((Number) row[0]).longValue();

            Map<String, Object> productEntry = productMap.get(productId);
            if (productEntry == null) {
                productEntry = new HashMap<>();
                productEntry.put("product_id", productId);
                productEntry.put("product_name", row[1]);
                productEntry.put("author", row[2]);
                productEntry.put("description", row[3]);
                productEntry.put("price", row[4]);
                productEntry.put("stock", row[5]);
                productEntry.put("categories", new HashSet<String>());
                productMap.put(productId, productEntry);
            }

            String catName = (String) row[6];
            if (catName != null) {
                ((Set<String>) productEntry.get("categories")).add(catName);
            }
        }

        return new ArrayList<>(productMap.values());
    }

    @Override
    public Page<ProductResponse> fullSearch(String name, String author, String language, List<Long> categoriesId
            , BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findAll(
                hasName(name)
                        .and(hasAuthor(author))
                        .and(hasLanguage(language))
                        .and(hasCategories(categoriesId))
                        .and(hasPriceBetween(minPrice, maxPrice)),
                pageable
        ).map(productMapper::toProductResponse);
    }


    @Override
    public List<Map<String, Object>> getTopSoldProducts(LocalDate startDate, LocalDate endDate) {
        return productJDBCRepository.getTopSoldProducts(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getPurchaseOrderItemBySupplierId(Long supplierId, LocalDate startDate, LocalDate endDate) {
        return productJDBCRepository.getPurchaseOrderItemBySupplierId(supplierId, startDate, endDate);
    }

    @Override
    public void updateStockAfterOrderSuccess(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setReserved(product.getReserved() - quantity);
        product.setSoldQuantity(product.getSoldQuantity() + quantity);

        productRepository.save(product);
    }

    @Override
    public void updateStockAfterOrderFailed(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        product.setReserved(product.getReserved() - quantity);
        product.setStock(product.getStock() + quantity);

        productRepository.save(product);
    }
}
