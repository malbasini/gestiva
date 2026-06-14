package com.gestiva.web.contact;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ContactService {

    private final JavaMailSender mailSender;

    @Value("support@vercert.org")
    private String contactTo;

    @Value("support@vercert.org")
    private String fromAddress;

    public ContactService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(ContactForm form) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(contactTo);
        message.setReplyTo(form.getEmail());
        message.setSubject("[Gestiva] Richiesta contatto - " + form.getSubject());
        message.setText(
                "Nome: " + form.getName() + "\n" +
                        "Email: " + form.getEmail() + "\n\n" +
                        "Messaggio:\n" + form.getMessage()
        );

        mailSender.send(message);
    }
}