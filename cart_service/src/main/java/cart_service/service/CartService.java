package cart_service.service;

import cart_service.dto.request.CartItemRequest;
import cart_service.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart();
    CartResponse addToCart(CartItemRequest request);
    CartResponse updateCartItem(CartItemRequest request);
    void removeFromCart(Long productId);
}
