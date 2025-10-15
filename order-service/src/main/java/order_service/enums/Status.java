package order_service.enums;

public enum Status {
    CREATED,            // Mới tạo đơn, chưa check stock
    STOCK_PENDING,      // Đang chờ xác nhận đủ hàng từ ProductService
    STOCK_RESERVED,     // Đã giữ hàng (reserved)
    OUT_OF_STOCK,       // Thiếu hàng → sẽ bị cancel
    PENDING_PAYMENT,    // Đủ hàng → chờ thanh toán
    PAID,               // Thanh toán thành công
    PAYMENT_FAILED,     // Thanh toán thất bại
    CANCELLED,          // Đơn bị hủy (do hết hàng, hết hạn thanh toán, hoặc người dùng hủy)
    SHIPPED             // Đã giao hàng
}
