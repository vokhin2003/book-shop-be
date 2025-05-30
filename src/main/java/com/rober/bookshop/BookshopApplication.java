package com.rober.bookshop;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication(exclude = {
//		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
//		org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class
//})
@SpringBootApplication
public class BookshopApplication {

	public static void main(String[] args) {

//		Dotenv dotenv = Dotenv.configure().load();
//
//		// Database
////		System.setProperty("DB_URL", dotenv.get("DB_URL"));
////		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
////		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
//
//		// JWT
////		System.setProperty("JWT_BASE64_SECRET", dotenv.get("JWT_BASE64_SECRET"));
//
//		// Cloudinary
//		System.setProperty("CLOUDINARY_CLOUD_NAME", dotenv.get("CLOUDINARY_CLOUD_NAME"));
//		System.setProperty("CLOUDINARY_API_KEY", dotenv.get("CLOUDINARY_API_KEY"));
//		System.setProperty("CLOUDINARY_API_SECRET", dotenv.get("CLOUDINARY_API_SECRET"));
//
//		// SendGrid
//		System.setProperty("SENDGRID_API_KEY", dotenv.get("SENDGRID_API_KEY"));
//		System.setProperty("SENDGRID_MAIL_FROM", dotenv.get("SENDGRID_MAIL_FROM"));
//		System.setProperty("SENDGRID_TEMPLATE_VERIFY", dotenv.get("SENDGRID_TEMPLATE_VERIFY"));
//
////		System.setProperty("SENDGRID_TEMPLATE_FORGOT", dotenv.get("SENDGRID_TEMPLATE_FORGOT"));


		SpringApplication.run(BookshopApplication.class, args);
	}

}
