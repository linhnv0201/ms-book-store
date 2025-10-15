package order_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import order_service.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    String code;

    @Column(name = "customer_id")
    Long customerId;

    @Column(name = "customer_email")
    String customerEmail;

    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Status status;

    // createDate (yyyyMMddHHmmss)
    @Column(name = "vnp_txn_ref", unique = true)
    String vnpTxnRef;  // để map với callback/querydr

    @Column(name = "vnp_create_date")
    String vnpCreateDate;  // yyyyMMddHHmmss

    BigDecimal totalAmount;

    @Column(columnDefinition = "TEXT")
    String note;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>(); // <-- quan trọng

}
