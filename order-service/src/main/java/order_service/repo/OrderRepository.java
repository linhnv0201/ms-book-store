package order_service.repo;

import order_service.entity.Order;
import order_service.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);
    boolean existsByCode(String code);
    List<Order> findByCustomerId(Long customerId);
    Optional<Order> findById(Long id);
//    Optional<Order> findByVnpTxnRef(String txnRef);
    List<Order> findByStatus(Status status);

}
