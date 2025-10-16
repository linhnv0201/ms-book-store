package payment_service.dto.response;

import lombok.Data;

@Data
public class VNPayQueryResponse {
    private String vnp_ResponseId;
    private String vnp_Command;
    private String vnp_ResponseCode;   // 00 = success
    private String vnp_Message;
    private String vnp_TxnRef;
    private String vnp_Amount;
    private String vnp_BankCode;
    private String vnp_TransactionNo;  // Mã giao dịch tại VNPAY
    private String vnp_TransactionStatus; // 00 = thành công
    private String vnp_OrderInfo;
    private String vnp_PayDate;
}
