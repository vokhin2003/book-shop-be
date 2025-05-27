package com.rober.bookshop.service;

public interface IEmailService {

    String send(String to, String subject, String body);
//    void sendVerifyMail(User user, Token token, String clientType);
    void sendVerificationEmail(String toEmail, String fullName, String verifyLink);

}
