package com.pricehawk.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendPriceDropAlert(String to, String productName, String currentPrice, String productUrl) {
        String subject = "Price Drop Alert: " + productName;
        String content = buildPriceDropEmail(productName, currentPrice, productUrl);

        sendEmail(to, subject, content);
    }

    private void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true = HTML

            mailSender.send(message);
            logger.info("Email sent to {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}", to, e);
        }
    }

    private String buildPriceDropEmail(String productName, String price, String url) {
        return "<div style=\"font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd;\">" +
               "<h2 style=\"color: #2c3e50;\">Price Drop Alert! 📉</h2>" +
               "<p>Good news! The price for <strong>" + productName + "</strong> has dropped.</p>" +
               "<p style=\"font-size: 18px; color: #27ae60;\">Current Price: <strong>" + price + "</strong></p>" +
               "<a href=\"" + url + "\" style=\"background-color: #3498db; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;\">Buy Now</a>" +
               "<p style=\"margin-top: 20px; font-size: 12px; color: #7f8c8d;\">You are receiving this because you added this item to your PriceHawk watchlist.</p>" +
               "</div>";
    }
}
