package eu.openminted.messaging.service.JMS;

import javax.jms.JMSException;

public class EmailMessage {
    private String recipient;
    private String subject;
    private String text;

    public void setRecipient(String recipient) throws JMSException {
        this.recipient = recipient;
    }

    public String getRecipient() throws JMSException {
        return this.recipient;
    }

    public void setSubject(String subject) throws JMSException {
        this.subject = subject;
    }

    public String getSubject() throws JMSException {
        return this.subject;
    }

    public void setText(String text) throws JMSException {
        this.text = text;
    }

    public String getText() throws JMSException {
        return this.text;
    }
}
