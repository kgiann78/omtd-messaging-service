package eu.openminted.messaging.service.email;

import org.apache.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.MimeMessage;

public class OmtdMessage extends MimeMessage {
    private static Logger log = Logger.getLogger(OmtdMessage.class.getName());

    public OmtdMessage(Session session) {
        super(session);
    }

    public void send(String subject, String text) {
        try {
            // Set Subject: header field
            this.setSubject("This is the Subject Line!");

            // Now set the actual message
            this.setText(text);

            // Send message
            Transport.send(this);
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            log.error("Error Sending message....", mex);
        }
    }
}
