package order_service.mapper;

import order_service.dto.response.OrderItemResponse;
import order_service.dto.response.OrderResponse;
import order_service.entity.Order;
import order_service.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "items", target = "items")
    OrderResponse toOrderResponse(Order order);

//    @Mapping(source = "product.name", target = "productName") // chỉ map tên product
    OrderItemResponse toOrderItemResponse(OrderItem item);
}
