package order_service.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import order_service.dto.request.OrderRequest;
import order_service.dto.response.ApiResponse;
import order_service.dto.response.OrderResponse;
import order_service.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    OrderService orderService;

    @GetMapping
    public ApiResponse<List<OrderResponse>> getAllOrders() {
        ApiResponse<List<OrderResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("All orders found");
        apiResponse.setResult(orderService.getAllOrders());
        return apiResponse;
    }

    @GetMapping("/mine")
    public ApiResponse<List<OrderResponse>> getMyOrders() {
        ApiResponse<List<OrderResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("All my orders");
        apiResponse.setResult(orderService.getMyOrders());
        return apiResponse;
    }

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        ApiResponse<OrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.createOrder(orderRequest));
        return apiResponse;
    }

//    @PostMapping("/cancel/{id}")
//    public ApiResponse<OrderResponse> cancelOrder(@PathVariable Long id) {
//        ApiResponse<OrderResponse> apiResponse = new ApiResponse<>();
//        apiResponse.setMessage("Order cancelled");
//        orderService.cancelOrder(id);
//        return apiResponse;
//    }

    @GetMapping("/spec/search")
    public ApiResponse<Page<OrderResponse>> searchOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59) : null;
        ApiResponse<Page<OrderResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.findAll(status, start, end, PageRequest.of(page, size)));
        return apiResponse;
    }
}
