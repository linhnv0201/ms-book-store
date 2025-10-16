package order_service.service;

import common_dto.dto.OrderPayRequest;
import order_service.dto.request.OrderRequest;
import order_service.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
//    void cancelOrder(Long orderId);
    OrderPayRequest getPendingPaymentInfo(Long orderId);
    List<OrderResponse> getAllOrders();
    List<OrderResponse> getMyOrders();
    Page<OrderResponse> findAll(String status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
