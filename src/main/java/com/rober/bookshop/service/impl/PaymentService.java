package com.rober.bookshop.service.impl;

import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.model.request.PaymentRequestDTO;
import com.rober.bookshop.model.response.PaymentResponseDTO;
import com.rober.bookshop.service.IOrderService;
import com.rober.bookshop.service.IPaymentService;
import com.rober.bookshop.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class PaymentService implements IPaymentService {

    private final IOrderService orderService;

    @Value("${rober.vnpay.vnp-tmn-code}")
    private String vnp_TmnCode;

    @Value("${rober.vnpay.vnp-hash-secret}")
    private String vnp_HashSecret;


    @Value("${rober.vnpay.url}")
    private String vnp_PayUrl;

    @Value("${rober.vnpay.return-url}")
    private String vnp_ReturnUrl;

    public PaymentService(IOrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public PaymentResponseDTO handleCreatePaymentUrl(PaymentRequestDTO dto, HttpServletRequest request){
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        String vnp_TxnRef = dto.getOrderId().toString();

        String ipAddr = VNPayUtil.getIpAddress(request);

        String orderInfo = "Paid for the order with id " + dto.getOrderId() + " . Total :" + dto.getAmount() + " VND";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);

        long vnpAmount = (long) (dto.getAmount() * 100);
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

        List<String> validFields = fieldNames.stream()
                .filter(k -> vnp_Params.get(k) != null && !vnp_Params.get(k).isEmpty())
                .toList();

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < validFields.size(); i++) {
            String fieldName = validFields.get(i);
            String fieldValue = vnp_Params.get(fieldName);

            hashData.append(fieldName)
                    .append('=')
                    .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                    .append('=')
                    .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

            if (i < validFields.size() - 1) {
                hashData.append('&');
                query.append('&');
            }
        }
        String queryUrl = query.toString();
        log.info("query url: {}", queryUrl);

        log.info("hash data: {}", hashData);
        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnp_HashSecret, hashData.toString());

        log.info("secure hash: {} ", vnp_SecureHash);

        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        PaymentResponseDTO result = new PaymentResponseDTO();
        result.setPaymentUrl(vnp_PayUrl + "?"+ queryUrl);


        return result;
    }

    @Override
    public Void handlePaymentResult(Map<String, String> params) {

        String vnp_SecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        params.remove("vnp_SecureHash");

        String signValue = VNPayUtil.hashAllFields(params, vnp_HashSecret);
        if (signValue.equals(vnp_SecureHash)) {
            String orderId = params.get("vnp_TxnRef");
            if (params.get("vnp_ResponseCode").equals("00")){

//                orderService.updateOrderStatusById(Long.parseLong(orderId), OrderStatusEnum.PAID);

            }
            else{
                throw new IdInvalidException("Paid Failed!!!");
            }
        }
        return null;
    }
}
