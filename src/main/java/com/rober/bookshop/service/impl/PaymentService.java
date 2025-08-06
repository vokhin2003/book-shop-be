package com.rober.bookshop.service.impl;

import com.rober.bookshop.enums.OrderStatus;
import com.rober.bookshop.enums.PaymentMethod;
import com.rober.bookshop.enums.TransactionStatus;
import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.model.entity.Order;
import com.rober.bookshop.model.entity.Transaction;
import com.rober.bookshop.model.request.PaymentRequestDTO;
import com.rober.bookshop.model.response.PaymentResponseDTO;
import com.rober.bookshop.model.response.PaymentResultDTO;
import com.rober.bookshop.repository.OrderRepository;
import com.rober.bookshop.repository.TransactionRepository;
import com.rober.bookshop.service.IOrderService;
import com.rober.bookshop.service.IPaymentService;
import com.rober.bookshop.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final IOrderService orderService;
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;

    @Value("${rober.vnpay.vnp-tmn-code}")
    private String vnp_TmnCode;

    @Value("${rober.vnpay.vnp-hash-secret}")
    private String vnp_HashSecret;


    @Value("${rober.vnpay.url}")
    private String vnp_PayUrl;

    @Value("${rober.vnpay.return-url}")
    private String vnp_WebReturnUrl;

    @Value("${rober.vnpay.app.return-url}")
    private String vnpAppReturnUrl;

    @Value("${rober.server.ip}")
    private String serverIp;

//    public PaymentService(IOrderService orderService) {
//        this.orderService = orderService;
//    }

    @Override
    @Transactional
    public PaymentResponseDTO handleCreatePaymentUrl(PaymentRequestDTO dto, HttpServletRequest request, String deviceType) {
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new IdInvalidException("Order with id = " + dto.getOrderId() + " not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IdInvalidException("Order is not in PENDING status");
        }

        if (dto.getPaymentMethod() != order.getPaymentMethod()) {
            throw new IdInvalidException("Payment method does not match order's payment method");
        }

        // Kiểm tra amount khớp với order.totalAmount
//        if (!dto.getAmount().equals(order.getTotalAmount())) {
//            throw new IdInvalidException("Payment amount does not match order total amount");
//        }

        // Kiểm tra amount khớp với order.totalAmount bằng compareTo
        if (dto.getAmount().compareTo(order.getTotalAmount()) != 0) {
            throw new IdInvalidException("Payment amount does not match order total amount");
        }

        // Lấy tất cả Transaction của Order
        List<Transaction> transactions = transactionRepository.findByOrder_Id(dto.getOrderId());

        // Kiểm tra đã có Transaction thành công chưa
        if (transactions.stream().anyMatch(t -> t.getStatus() == TransactionStatus.SUCCESS)) {
            throw new IdInvalidException("Order already paid successfully");
        }

        // Kiểm tra giới hạn 3 lần thanh toán
        if (transactions.size() >= 3) {
            // Tùy chọn: Tự động xóa đơn hàng hoặc yêu cầu xác nhận
//            order.setStatus(OrderStatus.CANCELLED);
//            orderRepository.save(order);
            throw new IdInvalidException("Maximum 3 payment attempts reached. Order has been cancelled.");
        }

//        if (transactions.size() >= 3) {
//            throw new IdInvalidException("Maximum 3 payment attempts reached. Please confirm to cancel the order.");
//        }

        Transaction transaction;

        // Tìm Transaction PENDING (thanh toán lần đầu)
        Optional<Transaction> pendingTransaction = transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.PENDING)
                .findFirst();

        if (pendingTransaction.isPresent()) {
            // Thanh toán lần đầu: Sử dụng Transaction PENDING hiện có
            transaction = pendingTransaction.get();
        } else {
            // Thanh toán lại: Tạo Transaction mới
            transaction = new Transaction();
//            String transactionId = dto.getOrderId() + "_" +Instant.now().toEpochMilli();

            String transactionId = dto.getOrderId() + "_" + deviceType +"_" +Instant.now().toEpochMilli();
            transaction.setTransactionId(transactionId);
            transaction.setAmount(dto.getAmount());
            transaction.setPaymentMethod(dto.getPaymentMethod());
            transaction.setStatus(TransactionStatus.PENDING);
            transaction.setTransactionDate(Instant.now());
            transaction.setOrder(order);
            transaction.setOrderInfo(String.format("Payment for order with id %d. Total: %.2f VND",
                    dto.getOrderId(), dto.getAmount()));
            transactionRepository.save(transaction);
        }

        PaymentResponseDTO result = new PaymentResponseDTO();
        result.setTransactionId(transaction.getTransactionId());
        result.setPaymentMethod(dto.getPaymentMethod());
        result.setStatus(TransactionStatus.PENDING);

        if (dto.getPaymentMethod() == PaymentMethod.VNPAY) {

            String vnp_ReturnUrl = vnp_WebReturnUrl;

            if (!deviceType.equalsIgnoreCase("web")){
                vnp_ReturnUrl = "http://" + serverIp + vnpAppReturnUrl;
            }
            log.info(">>>> Return url: {}", vnp_ReturnUrl);

            // Logic tạo URL thanh toán VNPay
            String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String orderType = "other";
            String vnp_TxnRef = transaction.getTransactionId();
            String ipAddr = VNPayUtil.getIpAddress(request);
            String orderInfo = transaction.getOrderInfo();

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            long vnpAmount = dto.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
            vnp_Params.put("vnp_Amount", String.valueOf(vnpAmount));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", orderType);
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", ipAddr);

            Instant now = Instant.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                    .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
            String vnp_CreateDate = formatter.format(now);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            Instant expireTime = now.plus(15, ChronoUnit.MINUTES);
            String vnp_ExpireDate = formatter.format(expireTime);
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName)
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                    if (i < fieldNames.size() - 1) {
                        hashData.append('&');
                        query.append('&');
                    }
                }
            }
            String queryUrl = query.toString();
            String vnp_SecureHash = VNPayUtil.hmacSHA512(vnp_HashSecret, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

            result.setPaymentUrl(vnp_PayUrl + "?" + queryUrl);
            result.setMessage("Redirecting to VNPay payment gateway");
        } else if (dto.getPaymentMethod() == PaymentMethod.COD) {
            result.setMessage("Order created successfully, awaiting delivery");
        } else {
            throw new IdInvalidException("Unsupported payment method: " + dto.getPaymentMethod());
        }

        return result;
    }

    @Override
    @Transactional
    public PaymentResultDTO handlePaymentResult(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        params.remove("vnp_SecureHash");

        String signValue = VNPayUtil.hashAllFields(params, vnp_HashSecret);
        if (!signValue.equals(vnp_SecureHash)) {
            throw new IdInvalidException("Invalid secure hash");
        }

        String transactionId = params.get("vnp_TxnRef");
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IdInvalidException("Transaction with id = " + transactionId + " not found"));

        PaymentResultDTO result = new PaymentResultDTO();
        result.setTransactionId(transactionId);

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            log.info("Transaction {} already processed with status {}", transactionId, transaction.getStatus());
            result.setStatus(transaction.getStatus());
            result.setMessage("Transaction already processed");
            return result;
        }

        // Kiểm tra Order không có Transaction thành công khác
        List<Transaction> transactions = transactionRepository.findByOrder_Id(transaction.getOrder().getId());
        if (transactions.stream().anyMatch(t -> t.getStatus() == TransactionStatus.SUCCESS && !t.getTransactionId().equals(transactionId))) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setResponseCode("99");
            transaction.setTransactionDate(Instant.now());
            transactionRepository.save(transaction);
            result.setStatus(TransactionStatus.FAILED);
            result.setMessage("Order already paid by another transaction");
            return result;
        }

        String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setProviderTransactionId(params.get("vnp_TransactionNo"));
            transaction.setBankCode(params.get("vnp_BankCode"));
            transaction.setResponseCode(responseCode);
//            transaction.setPaymentDetails("CardType: " + params.get("vnp_CardType"));
            transaction.setTransactionDate(Instant.parse(
                    params.get("vnp_PayDate").substring(0, 4) + "-" +
                            params.get("vnp_PayDate").substring(4, 6) + "-" +
                            params.get("vnp_PayDate").substring(6, 8) + "T" +
                            params.get("vnp_PayDate").substring(8, 10) + ":" +
                            params.get("vnp_PayDate").substring(10, 12) + ":" +
                            params.get("vnp_PayDate").substring(12, 14) + "+07:00"
            ));
            transactionRepository.save(transaction);

            String orderId = transactionId.split("_")[0];
            orderService.updateOrderStatusById(Long.parseLong(orderId), OrderStatus.CONFIRMED);

            result.setStatus(TransactionStatus.SUCCESS);
            result.setMessage("Payment successful");
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setResponseCode(responseCode);
            transaction.setTransactionDate(Instant.now());
            transactionRepository.save(transaction);

            // Kiểm tra số lần thanh toán
            if (transactions.size() >= 3) {
                Order order = transaction.getOrder();
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                result.setMessage("Payment failed! Order cancelled due to 3 failed attempts.");
            } else {
                result.setMessage("Payment failed! Response code: " + responseCode);
            }

            result.setStatus(TransactionStatus.FAILED);
//            result.setMessage("Payment failed! Response code: " + responseCode);
        }

        return result;
    }

    @Override
    public PaymentResultDTO getTransactionStatus(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IdInvalidException("Transaction with id = " + transactionId + " not found"));

        PaymentResultDTO result = new PaymentResultDTO();
        result.setTransactionId(transactionId);
        result.setStatus(transaction.getStatus());
//        result.setMessage(transaction.getStatus() == TransactionStatus.SUCCESS
//                ? "Payment successful"
//                : "Payment status: " + transaction.getStatus());

        result.setMessage(transaction.getStatus() == TransactionStatus.SUCCESS
                ? "Payment successful"
                : transaction.getStatus() == TransactionStatus.FAILED && transaction.getOrder().getStatus() == OrderStatus.CANCELLED
                ? "Payment failed! Order cancelled due to 3 failed attempts."
                : "Payment status: " + transaction.getStatus());
        return result;
    }
}
