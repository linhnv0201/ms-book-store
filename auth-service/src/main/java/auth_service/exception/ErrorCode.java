package auth_service.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    USER_EXISTED(999, "User already existed"),
    USER_NOT_FOUND(999, "User not found"),
    INVALID_PASSWORD(999, "At least 4 characters"),
    WRONG_PASSWORD(999, "Wrong password"),
    UNAUTHENTICATED(1001, "Unauthenticated"),
    INVALID_ROLE(1002, "Invalid role"),
    SUPPLIER_EXISTED(1003, "Supplier already existed"),
    CATEGORY_EXISTED(1003, "Category already existed"),
    SUPPLIER_NOT_FOUND(1003, "Supplier not found"),
    CATEGORY_NOT_FOUND(1003, "Category not found"),
    PRODUCT_EXISTED(1003, "Product already existed"),
    PRODUCT_NOT_EXIST(1003, "Product not existed"),
    PRODUCT_NOT_FOUND(1003, "Product not found"),
    NEGATIVE_QUANTITY(1004, "Negative quantity"),
    EMPTY_ORDER(1004, "Empty order"),
    ORDER_NOT_FOUND(1004, "Order not found"),
    CART_NOT_FOUND(1004, "Cart not found"),
    INSUFFICIENT_STOCK(1005, "Insufficient stock"),
    NO_PAYMENT_EXISTED(1005, "Payment already paid or cancelled"),
    INVALID_REQUEST(1006, "Invalid request"),
    INTERNAL_ERROR(1007, "Internal error"),
    REFRESH_TOKEN_NOT_FOUND(1008, "Refresh token not found"),
    TOKEN_GENERATION_FAILED(1009, "Token generation failed"),

    ;
    int code;
    String message;
}
