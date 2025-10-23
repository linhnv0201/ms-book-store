package product_service.cronjob;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import product_service.dto.response.ProductResponse;
import product_service.entity.Product;
import product_service.mapper.ProductMapper;
import product_service.repo.ProductRepository;
import product_service.service.ProductService;
import product_service.service.RedisCacheService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductCacheWarmUpJob {
    ProductMapper productMapper;
    ProductService productService;
    ProductRepository productRepository;
    RedisCacheService redisCacheService;

//    Khi thêm @Transactional, session Hibernate sẽ tồn tại trong suốt quá trình thực thi hàm,
//nên mapper có thể truy cập các field lazy mà không bị lỗi
    @Transactional
    @Scheduled(fixedRate = 600_000)
    public void warmUpTopSoldProducts(){
        System.out.println("[TopSoldProductCacheWarmUpJob] Running warm-up...");

        List<ProductResponse> topSoldProducts = getTop5Products();
        for (ProductResponse product : topSoldProducts) {
            String cacheKey = "product:" + product.getId();

            if (!redisCacheService.checkExistsKey(cacheKey)) {
                productService.getProduct(product.getId());
                System.out.println("   → Cached product id=" + product.getId());
            } else {
                System.out.println("   → Cache already exists for id=" + product.getId());
            }
        }

        System.out.println("[ProductCacheWarmUpJob] Done ✅");
    }

    public List<ProductResponse> getTop5Products() {
        List<Product> topProducts = productRepository.findTop5ByOrderBySoldQuantityDesc();

        List<ProductResponse> responses = new ArrayList<>();
        for (Product product : topProducts) {
            responses.add(productMapper.toProductResponse(product));
        }
        return responses;
    }

//    @Transactional
//    @Scheduled(fixedRate = 6_000)
//    public void warmUpAllProducts(){
//        System.out.println("[ProductCacheWarmUpJob] Running warm-up...");
//
//        List<Product> allProducts = productRepository.findAll();
//        List<ProductResponse> responses = new ArrayList<>();
//        for (Product product : allProducts) {
//            responses.add(productMapper.toProductResponse(product));
//        }
//        for (ProductResponse product : responses) {
//            String cacheKey = "product:" + product.getId();
//
//            if (!redisCacheService.checkExistsKey(cacheKey)) {
//                productService.getProduct(product.getId());
//                System.out.println("   → Cached product id=" + product.getId());
//            } else {
//                System.out.println("   → Cache already exists for id=" + product.getId());
//            }
//        }
//
//        System.out.println("[ProductCacheWarmUpJob] Done ✅");
//    }
}
