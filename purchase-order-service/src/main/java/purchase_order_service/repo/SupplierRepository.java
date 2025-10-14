package purchase_order_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import purchase_order_service.entity.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    boolean existsByName(String name);
}
