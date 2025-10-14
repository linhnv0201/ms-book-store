package purchase_order_service.service;


import purchase_order_service.dto.request.SupplierCreationAndUpdateRequest;
import purchase_order_service.dto.response.SupplierResponse;

import java.util.List;

public interface SupplierService {
    SupplierResponse createSupplier(SupplierCreationAndUpdateRequest request);
    SupplierResponse updateSupplier(Long id ,SupplierCreationAndUpdateRequest request);
    SupplierResponse getSupplier(Long id);
    List<SupplierResponse> getAllSuppliers();
    void deleteSupplier(Long id);
}
