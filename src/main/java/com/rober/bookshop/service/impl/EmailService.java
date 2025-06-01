package com.rober.bookshop.service.impl;

import com.rober.bookshop.exception.EmailException;
import com.rober.bookshop.service.IEmailService;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService implements IEmailService {

    private final SendGrid sendGrid;
    @Qualifier("from")
    private final String fromMail;

    @Value("${spring.sendgrid.template-id.verification}")
    private String verificationTemplateId;

    @Override
    public String send(String to, String subject, String body) {
        Email mailFrom = new Email(fromMail);

        Email mailTo = new Email(to, "Minh Tiến");

        Content content = new Content("text/plain", body);

        Mail mail  = new Mail(mailFrom, subject, mailTo, content);

        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            if(response.getStatusCode() == 202) {
                return "Send email successfully";
            }
            else{
                return "Failed to send email " + response.getBody();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendVerificationEmail(String toEmail, String fullName, String verifyLink) {
        try {
            // Lấy URL logo từ cloud
//            String logoUrl = "https://res.cloudinary.com/dcyexzesw/image/upload/v1746435662/28f29413-6909-4be5-8a87-b32a604a8c34_store2.jpg";
            String logoUrl = "https://res.cloudinary.com/dtfe2e0ey/image/upload/v1748637153/logo_igayak.png";

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
}
