package purchase_order_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String name;

    String email;

    String phone;

    @Column(columnDefinition = "TEXT")
    String address;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    List<PurchaseOrder> purchaseOrders;
}
