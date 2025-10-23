package cart_service.service.impl;

import cart_service.config.CurrentUserProvider;
import cart_service.config.ProductClient;
import cart_service.dto.request.CartItemRequest;
import cart_service.dto.response.ApiResponse;
import cart_service.dto.response.CartItemResponse;
import cart_service.dto.response.CartResponse;
import cart_service.dto.response.ProductResponse;
import cart_service.entity.Cart;
import cart_service.entity.CartItem;
import cart_service.exception.AppException;
import cart_service.exception.ErrorCode;
import cart_service.mapper.CartMapper;
import cart_service.repo.CartRepository;
import cart_service.service.CartService;
import cart_service.service.RedisCacheService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartServiceImpl implements CartService {

    CartRepository cartRepository;
    CartMapper cartMapper;
    CurrentUserProvider currentUserProvider;
    ProductClient productClient;
    RedisCacheService redisCacheService;

    @Override
    public CartResponse getCart() {
        Cart cart = getOrCreateCartEntity();

        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setCustomerId(cart.getCustomerId());
        response.setCustomerName(currentUserProvider.getUserDetailsFromRequest().getEmail());

        List<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            String key = "product:" + item.getProductId();
            ProductResponse product = null;

            try {
                // 1️⃣ Lấy từ cache trước
                // product = (ProductResponse) redisCacheService.getValue(key);

                // 2️⃣ Nếu không có cache thì gọi sang ProductService
                if (product == null) {
                    ApiResponse<ProductResponse> response1 = productClient.getProductById(item.getProductId());
                    if (response1 != null && response1.getResult() != null) {
                        product = response1.getResult();

                        // cache lại để lần sau dùng nhanh hơn
                        // redisCacheService.setValueWithTimeout(key, product, 6000, TimeUnit.SECONDS);
                    }
                }

                // 3️⃣ Nếu product null → bỏ qua item này
                if (product == null || product.getPrice() == null) {
                    System.err.println("⚠️ Không tìm thấy hoặc giá null cho productId: " + item.getProductId());
                    continue;
                }

                // 4️⃣ Tính toán và map sang response
                CartItemResponse itemRes = new CartItemResponse();
                itemRes.setProductId(product.getId());
                itemRes.setProductName(product.getName());
                itemRes.setUnitPrice(product.getPrice());
                itemRes.setQuantity(item.getQuantity());
                itemResponses.add(itemRes);

                total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi lấy productId " + item.getProductId() + ": " + e.getMessage());
            }
        }

        response.setItems(itemResponses);
        response.setTotalPrice(total);
        return response;
    }



    // Luôn trả về Cart. Nếu user chưa có thì tạo mới.
    public Cart getOrCreateCartEntity() {
        return cartRepository.findByCustomerId(currentUserProvider.getUserDetailsFromRequest().getUserId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomerId(currentUserProvider.getUserDetailsFromRequest().getUserId());
                    return cartRepository.save(newCart);
                });
    }

    @Override
    public CartResponse addToCart(CartItemRequest request) {
        Cart cart = getOrCreateCartEntity();

        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(ci -> ci.getProductId().equals(request.getProductId()))
                .findFirst();

        itemOpt.ifPresentOrElse(
                item -> item.setQuantity(item.getQuantity() + request.getQuantity()),
                () -> {
                    var item = new CartItem();
                    item.setCart(cart);
                    item.setProductId(request.getProductId());
                    item.setQuantity(request.getQuantity());
                    cart.getItems().add(item);
                }
        );
            cartRepository.save(cart);
        CartResponse cartResponse = cartMapper.toCartResponse(cart);

//        BigDecimal total = cart.getItems().stream()
//                .map(item -> item.getProduct().getPrice()
//                        .multiply(BigDecimal.valueOf(item.getQuantity())))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//        cartResponse.setTotalPrice(total);
        return cartResponse;
    }

    @Override
    public CartResponse updateCartItem(CartItemRequest request) {
        if (request.getProductId() == null) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (request.getQuantity() <= 0) {
            throw new AppException(ErrorCode.NEGATIVE_QUANTITY);
        }

        Cart cart = getOrCreateCartEntity();

        // Tìm item trong cart
        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (request.getQuantity() <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(request.getQuantity());
        }

            cartRepository.save(cart);
        CartResponse cartResponse = cartMapper.toCartResponse(cart);

//        BigDecimal total = cart.getItems().stream()
//                .map(item2 -> item2.getProduct().getPrice()
//                        .multiply(BigDecimal.valueOf(item2.getQuantity())))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//        cartResponse.setTotalPrice(total);
        return cartResponse;
    }

    @Override
    public void removeFromCart(Long productId) {
        Cart cart = getOrCreateCartEntity();
        cart.getItems().removeIf(ci -> ci.getProductId().equals(productId));
        cartRepository.save(cart);
    }

}
