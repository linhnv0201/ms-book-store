package payment_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "customer_payments")
public class CustomerPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long orderId;

    LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentMethod paymentMethod;

    @Column(nullable = false)
    BigDecimal amount;

    // createDate (yyyyMMddHHmmss)
    @Column(name = "vnp_txn_ref", unique = true)
    String vnpTxnRef;  // để map với callback/querydr

    @Column(name = "vnp_create_date")
    String vnpCreateDate;

    @Enumerated(EnumType.STRING)
    Status status;

    public enum PaymentMethod {
        CASH, BANK_TRANSFER, OTHER
    }

    public enum Status {
        SUCCESS, FAILED, PENDING
    }
}
