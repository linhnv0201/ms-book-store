package product_service.process;

import lombok.RequiredArgsConstructor;
import product_service.dto.response.ProductSummaryResponse;
import product_service.service.ProductService;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class ProductProcess implements Runnable {
    private final ProductService productService;
    private final Long productId;
    public List<ProductSummaryResponse> result;

    @Override
    public void run() {
        try {
            System.out.println("[ProductProcess] Running background task for product " + productId);
            result = productService.getRelatedProductSummaryByProductId(productId);
            System.out.println("[ProductProcess] Finished background task for product " + productId + ", cached " +
                    (result != null ? result.size() : 0) + " related products." +" at " + LocalDateTime.now());
        } catch (Exception e) {
            System.out.println("[ProductProcess] Error: " + e.getMessage());
        }
    }
}
