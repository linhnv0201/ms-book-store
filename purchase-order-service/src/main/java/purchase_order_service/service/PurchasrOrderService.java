package purchase_order_service.service;


import purchase_order_service.dto.request.PurchaseOrderCreationRequest;
import purchase_order_service.dto.response.PurchaseOrderResponse;

import java.util.List;

public interface PurchasrOrderService {
    PurchaseOrderResponse createPurchaseOrder(PurchaseOrderCreationRequest request);
    List<PurchaseOrderResponse> getAll();
    PurchaseOrderResponse getById(Long id);

}
