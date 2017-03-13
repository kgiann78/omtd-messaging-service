package eu.openminted.messaging.service.JMS;

import eu.openminted.messaging.service.email.EmailService;
import eu.openminted.messaging.service.email.OmtdMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JMSConsumer implements ExceptionListener, MessageListener {
    private static Logger log = Logger.getLogger(JMSConsumer.class.getName());

    @org.springframework.beans.factory.annotation.Value("${jms.content.corpus.topic}")
    private String topicName;

    @org.springframework.beans.factory.annotation.Value("${mail.port}")
    private String mailPort;

    @org.springframework.beans.factory.annotation.Value("${mail.sender.id}")
    private String mailSenderId;

    @org.springframework.beans.factory.annotation.Value("${mail.username}")
    private String mailUsername;

    @org.springframework.beans.factory.annotation.Value("${mail.password}")
    private String mailPassword;

    @org.springframework.beans.factory.annotation.Value("${mail.smtp.host}")
    private String mailSmtpHost;

    @Autowired
    private ActiveMQConnectionFactory connectionFactory;

    @Autowired
    private EmailService emailService;

    public void listen() {
        try {
            // Create a Connection
            Connection connection = connectionFactory.createConnection();
            // Set unique clientID to connection prior to connect
            connection.setClientID(Integer.toString(this.hashCode()));
            connection.start();

            connection.setExceptionListener(this);

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the Topic
            Topic topic = session.createTopic(topicName);

            // Create a TopicSubscriber from the Session to the Topic
            TopicSubscriber subscriber = session.createDurableSubscriber(topic, "SUBSCRIBER");
            subscriber.setMessageListener(this);

        } catch (Exception e) {
            log.error("Caught Exception: " + e);
        }
    }

    public synchronized void onException(JMSException ex) {
        log.error("JMS Exception occurred.  Shutting down client.");
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            try {
                TextMessage textMessage = (TextMessage) message;
                String text = textMessage.getText();
                log.info("Message Received: " + text);

                if (text.contains("email")) {
                    // Recipient's email ID needs to be mentioned.
                    String to = "";
                    Matcher match = Pattern.compile("^email<(.*)>(.*$)").matcher(text);
                    if (match.find()) {
                        to = match.group(1);
                        System.out.println("\n\n" + to + "\n\n");
                        text = match.group(2);
                    }

                    if (to.isEmpty()) return;

                    // Get system properties
                    Properties properties = System.getProperties();

                    // Setup mail server
                    properties.put("mail.smtp.starttls.enable", "true");
                    properties.put("mail.smtp.auth", "true");
                    properties.setProperty("mail.smtp.host", mailSmtpHost);
                    properties.put("mail.port", mailPort);

                    // Get the default Session object.
                    javax.mail.Session session = javax.mail.Session.getDefaultInstance(properties, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(mailUsername, mailPassword);
                        }
                    });

                    try {
                        // Create a default MimeMessage object.
                        MimeMessage emailMessage = new MimeMessage(session);

                        // Set From: header field of the header.
                        emailMessage.setFrom(new InternetAddress(mailSenderId));

                        // Set To: header field of the header.
                        emailMessage.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));

                        // Set Subject: header field
                        emailMessage.setSubject("This is the Subject Line!");

                        // Now set the actual message
                        emailMessage.setText(text);

                        // Send message
                        Transport.send(emailMessage);
                        System.out.println("Sent message successfully....");
                    }catch (MessagingException mex) {
                        log.error("Error Sending message....", mex);
                    }
                }
            } catch (JMSException e) {
                log.error("Error Receiving Message", e);
            }
        } else if (message instanceof EmailMessage) {
            try {
                EmailMessage emailMessage = (EmailMessage) message;
                String recipient = emailMessage.getRecipient();
                String subject = emailMessage.getSubject();
                String text = emailMessage.getText();

                OmtdMessage omtdMessage = emailService.createMessage(recipient);
                omtdMessage.send(subject, text);
            } catch (JMSException e) {
                log.error("Error Receiving Email Message", e);
            }
        } else {
            log.info("Message Received: " + message);
        }
    }
}

