package cart_service.mapper;

import cart_service.dto.response.CartItemResponse;
import cart_service.dto.response.CartResponse;
import cart_service.entity.Cart;
import cart_service.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface CartMapper {
//    @Mapping(source = "customer.fullname", target = "customerName")
    CartResponse toCartResponse(Cart cart);
    CartItemResponse toCartItemResponse(CartItem cartItem);
}
