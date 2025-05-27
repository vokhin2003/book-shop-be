package com.rober.bookshop.config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendGridConfiguration {

    @Value("${spring.sendgrid.api-key}")
    private String apiKey;

    @Value("${spring.sendgrid.mail-from}")
    String mailFromStr;

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(apiKey);
    }

    @Bean(name = "from")
    public String getMailFromStr(){
        return mailFromStr;
    }

}
