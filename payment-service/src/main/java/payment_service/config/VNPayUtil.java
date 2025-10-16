package payment_service.config;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

public class VNPayUtil {

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP:" + e.getMessage();
        }
        return ipAdress;
    }

    public static String getPaymentURL(Map<String, String> paramsMap, boolean encodeKey) {
        return paramsMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .sorted(Map.Entry.comparingByKey())
                .map(entry ->
                        (encodeKey ? URLEncoder.encode(entry.getKey(),
                                StandardCharsets.US_ASCII)
                                : entry.getKey()) + "=" +
                                URLEncoder.encode(entry.getValue()
                                        , StandardCharsets.US_ASCII))
                .collect(Collectors.joining("&"));
    }

    public static String generateTxnRef(Long orderId) {
        // format theo yyyyMMddHHmmss cho dễ trace
        String timestamp = new java.text.SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date());
        return orderId + "-" + timestamp;
    }

    public static String hashAllFields(Map<String, String> fields, String secretKey) {
        // 1. Cấu trúc thứ tự tham số theo tài liệu
        String[] orderedKeys = new String[]{
                "vnp_RequestId",
                "vnp_Version",
                "vnp_Command",
                "vnp_TmnCode",
                "vnp_TxnRef",
                "vnp_TransactionDate",
                "vnp_CreateDate",
                "vnp_IpAddr",
                "vnp_OrderInfo"
        };

        // 2. Nối giá trị bằng '|'
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < orderedKeys.length; i++) {
            String key = orderedKeys[i];
            String value = fields.get(key);
            if (value == null) value = "";
            sb.append(value);
            if (i < orderedKeys.length - 1) {
                sb.append("|");
            }
        }

        // 3. Tính HMAC SHA512
        return hmacSHA512(secretKey, sb.toString());
    }


    public static String buildQueryString(Map<String, String> params) {
        return params.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }


}