package purchase_order_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import purchase_order_service.entity.PurchaseOrder;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findBySupplierId(Long supplierId);
    boolean existsByCode(String code);
}
