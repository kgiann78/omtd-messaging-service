package eu.openminted.messaging.service.email;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.InternetAddress;
import java.util.Properties;

@Component
public class EmailService {
    private static Logger log = Logger.getLogger(EmailService.class.getName());

    @Value("${mail.port}")
    private String mailPort;

    @Value("${mail.sender.id}")
    private String mailSenderId;

    @Value("${mail.username}")
    private String mailUsername;

    @Value("${mail.password}")
    private String mailPassword;

    @Value("${mail.smtp.host}")
    private String mailSmtpHost;

    private Properties properties;

    @PostConstruct
    private void init() {
        // Setup mail server
        properties = System.getProperties();
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.host", mailSmtpHost);
        properties.put("mail.port", mailPort);
    }

    public OmtdMessage createMessage(String recipient) {
        OmtdMessage emailMessage = null;

        // Get the default Session object.
        javax.mail.Session session = javax.mail.Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailUsername, mailPassword);
            }
        });

        try {
            // Create a default MimeMessage object.
            emailMessage = new OmtdMessage(session);

            // Set From: header field of the header.
            emailMessage.setFrom(new InternetAddress(mailSenderId));

            // Set To: header field of the header.
            emailMessage.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(recipient));

        } catch (Exception e) {
            log.error("error creating email message", e);
        }

        return emailMessage;
    }
}
