package purchase_order_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import purchase_order_service.dto.request.SupplierCreationAndUpdateRequest;
import purchase_order_service.dto.response.SupplierResponse;
import purchase_order_service.entity.Supplier;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    Supplier toCreateSupplier(SupplierCreationAndUpdateRequest request);
    SupplierResponse toSupplierResponse(Supplier supplier);
    void toUpdateSupplier(@MappingTarget Supplier supplier, SupplierCreationAndUpdateRequest request);

}
