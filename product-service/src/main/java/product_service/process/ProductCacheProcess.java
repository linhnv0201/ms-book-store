package product_service.process;

import product_service.entity.Product;
import product_service.service.RedisCacheService;

public class ProductCacheProcess implements Runnable{
    private RedisCacheService redisCacheService;
    private final String CACHE_KEY = "product_cache_key";

    public ProductCacheProcess (RedisCacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (redisCacheService.checkExistsKey(CACHE_KEY)) {
                    Product customer = (Product) redisCacheService.rPop(CACHE_KEY);
                    redisCacheService.setValue("customer_"+customer.getId(), customer);
                } else {
                    Thread.sleep(100);
                }
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }
}
