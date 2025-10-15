package order_service.specification;

import order_service.entity.Order;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class OrderSpecification {
    public static Specification<Order> hasStatus(String status) {
        return (root, query, builder) ->
                (status == null || status.isEmpty()) ? null :
                        builder.equal(root.get("status"), status);
    }

    public static Specification<Order> createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime finalStart = startDate;
        LocalDateTime finalEnd = (endDate == null && startDate != null) ? LocalDateTime.now() : endDate;

        return (root, query, builder) -> {
            if (finalStart == null && finalEnd == null) {
                return null; // all time
            }

            if (finalStart != null && finalEnd != null) {
                return builder.between(root.get("createdAt"), finalStart, finalEnd);
            } else if (finalStart != null) {
                return builder.greaterThanOrEqualTo(root.get("createdAt"), finalStart);
            } else {
                return builder.lessThanOrEqualTo(root.get("createdAt"), finalEnd);
            }
        };
    }



}
