package com.rober.bookshop.service.impl;

import com.rober.bookshop.exception.EmailException;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.EmailOrderItemDTO;
import com.rober.bookshop.model.response.OrderResponseDTO;
import com.rober.bookshop.service.IEmailService;
import com.rober.bookshop.util.StringUtil;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService implements IEmailService {

    private final SendGrid sendGrid;
    @Qualifier("from")
    private final String fromMail;

    @Value("${spring.bookshop.logo}")
    String logoUrl;

    @Value("${spring.sendgrid.template-id.verification}")
    private String verificationTemplateId;

    @Value("${spring.sendgrid.template-id.order}")
    private String orderSuccessTemplateId;


    @Override
    public String send(String to, String subject, String body) {
        Email mailFrom = new Email(fromMail);

        Email mailTo = new Email(to, "Minh Tiến");

        Content content = new Content("text/plain", body);

        Mail mail = new Mail(mailFrom, subject, mailTo, content);

        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            if (response.getStatusCode() == 202) {
                return "Send email successfully";
            } else {
                return "Failed to send email " + response.getBody();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendVerificationEmail(String toEmail, String fullName, String verifyLink) {
        try {

            // Tạo email
            Email from = new Email(fromMail, "BookShop");
            Email to = new Email(toEmail);
            Mail mail = new Mail();
            mail.setFrom(from);
            mail.setTemplateId(verificationTemplateId);
//            mail.setSubject("Verify Your BookShop Account");

            // Tạo personalization để truyền dữ liệu động
            Personalization personalization = new Personalization();
            personalization.addTo(to);
            personalization.addDynamicTemplateData("fullName", fullName);
            personalization.addDynamicTemplateData("verifyLink", verifyLink);
            personalization.addDynamicTemplateData("logoUrl", logoUrl);
            personalization.addDynamicTemplateData("subject", "Verify Your BookShop Account"); // Truyền subject
            mail.addPersonalization(personalization);

            // Gửi email qua SendGrid
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            request.getHeaders().put("Connection", "close");
            Response response = sendGrid.api(request);

            log.debug("SendGrid response: Status={}, Body={}", response.getStatusCode(), response.getBody());

            if (response.getStatusCode() >= 400) {
                String errorMessage = "Failed to send verification email: " + response.getBody();
                log.error(errorMessage);
                throw new EmailException(errorMessage);
            } else {
                log.info("Verification email sent successfully to: {}", toEmail);
            }
        } catch (IOException e) {
            log.error("Error sending verification email to {}: {}", toEmail, e.getMessage());
            throw new EmailException("Error sending verification email");
        }
    }

    @Override
    public void sendOrderSuccessEmail(User user, OrderResponseDTO dto) {
        try {

            // Tạo email
            Email from = new Email(fromMail, "BookShop");
            Email to = new Email(user.getEmail());
            Mail mail = new Mail();
            mail.setFrom(from);
            mail.setTemplateId(orderSuccessTemplateId);
//            mail.setSubject("Verify Your BookShop Account");

            // Tạo personalization để truyền dữ liệu động
            Personalization personalization = new Personalization();
            personalization.addTo(to);


            personalization.addDynamicTemplateData("customerName", user.getFullName());
            personalization.addDynamicTemplateData("orderId", dto.getId().toString());
            personalization.addDynamicTemplateData("orderStatus", dto.getStatus().toString());
            personalization.addDynamicTemplateData("orderTime", StringUtil.formatTime(dto.getCreatedAt()));
            personalization.addDynamicTemplateData("paymentMethod", dto.getPaymentMethod());
            personalization.addDynamicTemplateData("orderTotal", StringUtil.formatPrice(dto.getTotalAmount()) + " VND");
            personalization.addDynamicTemplateData("recipientName", dto.getFullName());
            personalization.addDynamicTemplateData("recipientPhone", dto.getPhone());
            personalization.addDynamicTemplateData("shippingAddress", dto.getShippingAddress());
            personalization.addDynamicTemplateData("orderItems", createOrderItemEmail(dto));
            personalization.addDynamicTemplateData("logo", logoUrl);


            personalization.addDynamicTemplateData("subject", "Order Successfully Notification");

//            personalization.addDynamicTemplateData("orderData", dto);
//            personalization.addDynamicTemplateData("subject", "Order Successfully Notification"); // Truyền subject
            mail.addPersonalization(personalization);

            // Gửi email qua SendGrid
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            request.getHeaders().put("Connection", "close");

            Response response = sendGrid.api(request);

            log.debug("SendGrid response: Status={}, Body={}", response.getStatusCode(), response.getBody());

            if (response.getStatusCode() >= 400) {
                String errorMessage = "Failed to send verification email: " + response.getBody();
                log.error(errorMessage);
                throw new EmailException(errorMessage);
            } else {
                log.info("Verification email sent successfully to: {}", user.getEmail());
            }
        } catch (IOException e) {
            log.error("Error sending verification email to {}: {}", user.getEmail(), e.getMessage());
            throw new EmailException("Error sending verification email");
        }
    }

    private List<EmailOrderItemDTO> createOrderItemEmail(OrderResponseDTO respDTO) {

        List<EmailOrderItemDTO> itemEmails = new ArrayList<>();

        respDTO.getOrderItems().forEach(item -> {
            EmailOrderItemDTO itemEmail = EmailOrderItemDTO.builder()
                    .thumbnail(item.getBook().getThumbnail())
                    .price(StringUtil.formatPrice(item.getPrice()) + " VND")
                    .title(item.getBook().getTitle())
                    .quantity(item.getQuantity().toString())
                    .build();
            itemEmails.add(itemEmail);
        });
        return itemEmails;
    }
}
