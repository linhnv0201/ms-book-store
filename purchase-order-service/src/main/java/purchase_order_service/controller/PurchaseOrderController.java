package purchase_order_service.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import purchase_order_service.dto.request.PurchaseOrderCreationRequest;
import purchase_order_service.dto.response.ApiResponse;
import purchase_order_service.dto.response.PurchaseOrderResponse;
import purchase_order_service.service.PurchasrOrderService;


import java.util.List;

@Slf4j
@RestController
@RequestMapping("/purchase-orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PurchaseOrderController {

    PurchasrOrderService purchaseOrderService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<PurchaseOrderResponse> createPurchaseOrder(
            @RequestBody PurchaseOrderCreationRequest request) {
        ApiResponse<PurchaseOrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(purchaseOrderService.createPurchaseOrder(request));
        return apiResponse;
    }

        @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @GetMapping
    public ApiResponse<List<PurchaseOrderResponse>> getAllOrders() {
        ApiResponse<List<PurchaseOrderResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(purchaseOrderService.getAll());
        return apiResponse;
    }

        @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @GetMapping("/{id}")
    public ApiResponse<PurchaseOrderResponse> getOrderById(@PathVariable Long id) {
        ApiResponse<PurchaseOrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(purchaseOrderService.getById(id));
        return apiResponse;
    }
}
