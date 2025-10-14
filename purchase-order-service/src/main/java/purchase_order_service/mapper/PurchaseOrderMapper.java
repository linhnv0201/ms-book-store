package purchase_order_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import purchase_order_service.dto.response.PurchaseOrderItemResponse;
import purchase_order_service.dto.response.PurchaseOrderResponse;
import purchase_order_service.entity.PurchaseOrder;
import purchase_order_service.entity.PurchaseOrderItem;

@Mapper(componentModel = "spring")
public interface PurchaseOrderMapper {
    // tip: source lấy trong entity rồi lấy tên field , map sang target là tên trong response
    @Mapping(source = "items", target = "items")
    @Mapping(source = "createdByEmail", target = "createdBy") // map sang field trong DTO
    @Mapping(source = "supplier.name", target = "supplierName")
    PurchaseOrderResponse toResponse(PurchaseOrder purchaseOrder);

//    @Mapping(source = "product.id", target = "productId") // chỉ map tên product
    PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem item);
}
