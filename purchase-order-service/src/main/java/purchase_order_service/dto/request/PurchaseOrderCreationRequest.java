package purchase_order_service.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseOrderCreationRequest {
    Long supplierId;
    String note;
    List<PurchaseOrderItemRequest> items;
}
