package purchase_order_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import purchase_order_service.entity.PurchaseOrderItem;

import java.util.List;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {
    List<PurchaseOrderItem> findByPurchaseOrderId(Long purchaseOrderId);
}
