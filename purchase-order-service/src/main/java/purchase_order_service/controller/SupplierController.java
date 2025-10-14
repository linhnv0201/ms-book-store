package purchase_order_service.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import purchase_order_service.dto.request.SupplierCreationAndUpdateRequest;
import purchase_order_service.dto.response.ApiResponse;
import purchase_order_service.dto.response.SupplierResponse;
import purchase_order_service.service.SupplierService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/purchase-orders/suppliers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SupplierController {

    SupplierService supplierService;

    // Create supplier
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SupplierResponse> createSupplier(@RequestBody SupplierCreationAndUpdateRequest request) {
        ApiResponse<SupplierResponse> response = new ApiResponse<>();
        response.setResult(supplierService.createSupplier(request));
        return response;
    }

    // Update supplier
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SupplierResponse> updateSupplier(
            @PathVariable Long id,
            @RequestBody SupplierCreationAndUpdateRequest request) {
        ApiResponse<SupplierResponse> response = new ApiResponse<>();
        response.setResult(supplierService.updateSupplier(id, request));
        return response;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @GetMapping("/{id}")
    public ApiResponse<SupplierResponse> getSupplier(@PathVariable Long id) {
        ApiResponse<SupplierResponse> response = new ApiResponse<>();
        response.setResult(supplierService.getSupplier(id));
        return response;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @GetMapping
    public ApiResponse<List<SupplierResponse>> getAllSuppliers() {
        ApiResponse<List<SupplierResponse>> response = new ApiResponse<>();
        response.setResult(supplierService.getAllSuppliers());
        return response;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Supplier deleted successfully");
        return response;
    }
}
