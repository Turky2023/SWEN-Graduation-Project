package com.gradproject.service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String DISPLAY_NAME = "Graduation Project Portal";
    private static final String REPLY_TO = "noreply@gradproject.com";

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            helper.setFrom(new InternetAddress(fromAddress, DISPLAY_NAME));
            helper.setReplyTo(REPLY_TO);
            mailSender.send(message);
            logger.info("AUDIT - Email sent to: {} subject: {}", to, subject);
        } catch (Exception e) {
            logger.error("AUDIT - Failed to send email to: {} error: {}", to, e.getMessage());
        }
    }
}
