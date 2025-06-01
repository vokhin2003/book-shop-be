package com.rober.bookshop.service;

import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.response.OrderResponseDTO;

public interface IEmailService {

    String send(String to, String subject, String body);
//    void sendVerifyMail(User user, Token token, String clientType);
    void sendVerificationEmail(String toEmail, String fullName, String verifyLink);
    void sendOrderSuccessEmail(User user, OrderResponseDTO dto);
}
