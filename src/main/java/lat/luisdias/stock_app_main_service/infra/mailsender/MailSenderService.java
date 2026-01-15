package lat.luisdias.stock_app_main_service.security.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class MailSenderService {
    @Value("${spring.mail.properties.from}")
    private String mailFrom;
    private final Logger logger = LoggerFactory.getLogger(MailSenderService.class);

    private final JavaMailSender mailSender;

    public MailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendMail(String to, String subject, String qrCodeUrl) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            String emailBody = Files.readString(Path.of("src/main/resources/static/email-enable-2fa.html"));
            String body = emailBody.replace("{{qrcode_url}}", qrCodeUrl);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setFrom(mailFrom);
        } catch (MessagingException | IOException e) {
            logger.error("Error sending the email: {}", e.getMessage());
        }
        mailSender.send(mimeMessage);
    }
}
