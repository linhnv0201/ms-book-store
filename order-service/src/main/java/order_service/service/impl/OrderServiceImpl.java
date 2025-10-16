package order_service.service.impl;

import common_dto.dto.OrderCreatedEvent;
import common_dto.dto.OrderPayRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import order_service.config.CurrentUserProvider;
import order_service.dto.request.OrderItemRequest;
import order_service.dto.request.OrderRequest;
import order_service.dto.response.CurrentUserResponse;
import order_service.dto.response.OrderResponse;
import order_service.entity.Order;
import order_service.entity.OrderItem;
import order_service.enums.Status;
import order_service.exception.AppException;
import order_service.exception.ErrorCode;
import order_service.mapper.OrderMapper;
import order_service.repo.OrderItemRepository;
import order_service.repo.OrderRepository;
import order_service.service.KafkaProducerService;
import order_service.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static order_service.specification.OrderSpecification.createdBetween;
import static order_service.specification.OrderSpecification.hasStatus;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    OrderMapper orderMapper;
    CurrentUserProvider currentUserProvider;
    KafkaProducerService kafkaProducerService;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_ORDER);
        }

        CurrentUserResponse currentUser = currentUserProvider.getUserDetailsFromRequest();

        Order order = new Order();
        order.setCustomerId(currentUser.getUserId());
        order.setCustomerEmail(currentUser.getEmail());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(Status.STOCK_PENDING);
        order.setCode(generateOrderCode());
        order.setNote(request.getNote());

        for (OrderItemRequest itemReq : request.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(itemReq.getProductId());
            orderItem.setQuantity(itemReq.getQuantity());

            order.getItems().add(orderItem);
        }
        orderRepository.save(order);
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
        orderCreatedEvent.setOrderId(order.getId());
        orderCreatedEvent.setItems(
                order.getItems().stream().map(orderItem -> {
                    OrderCreatedEvent.OrderItemEvent orderItemEvent = new OrderCreatedEvent.OrderItemEvent();
                    orderItemEvent.setProductId(orderItem.getProductId());
                    orderItemEvent.setQuantity(orderItem.getQuantity());
                    return orderItemEvent;
                }).toList()
        );
        kafkaProducerService.sendOrderCreatedEvent(orderCreatedEvent);
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> getMyOrders() {
        CurrentUserResponse currentUser = currentUserProvider.getUserDetailsFromRequest();
        List<Order> orders = orderRepository.findByCustomerId(currentUser.getUserId());
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    public Page<OrderResponse> findAll(String status, LocalDateTime startDate,
                                       LocalDateTime endDate, Pageable pageable) {
        return orderRepository.findAll(
                        hasStatus(status)
                                .and(createdBetween(startDate, endDate)),
                        pageable)
                .map(orderMapper::toOrderResponse);
    }

    @Override
    public OrderPayRequest getPendingPaymentInfo(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() != Status.PENDING_PAYMENT) {
            throw new AppException(ErrorCode.NO_PAYMENT_EXISTED);
        }

        return OrderPayRequest.builder()
                .orderId(order.getId())
                .totalAmount(order.getTotalAmount())
                .build();
    }

    private String generateOrderCode() {
        // 1. Lấy ngày hiện tại dạng YYYYMMDD
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 2. Sinh 4 chữ số ngẫu nhiên
        int randomPart = (int) (Math.random() * 10000); // 0-9999
        String randomPartStr = String.format("%04d", randomPart);

        // 3. Kết hợp ngày + random
        String code = "ORDER" + datePart + randomPartStr;

        // 4. Kiểm tra trùng với DB (nếu muốn thật sự an toàn)
        while (orderRepository.existsByCode(code)) {
            randomPart = (int) (Math.random() * 10000);
            randomPartStr = String.format("%04d", randomPart);
            code = "ORDER" + datePart + randomPartStr;
        }

        return code;
    }

}

