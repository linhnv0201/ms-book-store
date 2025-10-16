package payment_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import payment_service.entity.CustomerPayment;

import java.util.List;

public interface CustomerPaymentRepository extends JpaRepository<CustomerPayment, Long> {
    List<CustomerPayment> findByOrderId(Long orderId);
    CustomerPayment findByVnpTxnRef(String vnpTxnRef);
//    boolean existsByOrder(Order order);
    boolean existsByOrderId(Long orderId);
}
