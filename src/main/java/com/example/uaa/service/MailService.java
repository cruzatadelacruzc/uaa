package com.example.uaa.service;

import com.example.uaa.config.AppProperties;
import com.example.uaa.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Slf4j
@Service
public class MailService {
    
    private final AppProperties properties;

    private final MessageSource messageSource;

    private final JavaMailSender javaMailSender;

    private final SpringTemplateEngine templateEngine;


    public MailService(AppProperties properties,
                       JavaMailSender javaMailSender,
                       MessageSource messageSource,
                       SpringTemplateEngine templateEngine) {
        this.properties = properties;
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendActivationEmail(User user) {
        log.debug("Sending activation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "email/activationEmail", "email.activation.title");
    }

    @Async
    public void sendEmailFromTemplate(User user, String templateName, String titleKey) {
        if(user.getEmail() == null){
            log.debug("Email doesn't exist for user '{}'", user.getUsername());
            return;
        }

        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable("user", user);
        context.setVariable("baseUrl", properties.getMail().getBaseUrl());
        String content = templateEngine.process(templateName, context);
        String subject  = messageSource.getMessage(titleKey, null, locale);
        sendEmail(user.getEmail(), subject,content,false,true);
    }

    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        log.debug("Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
                isMultipart, isHtml, to, subject, content);
        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setSubject(subject);
            message.setFrom(properties.getMail().getFrom());
            message.setText(content, isHtml);
            javaMailSender.send(mimeMessage);
            log.debug("Sent email to User '{}'", to);
        } catch (MailException | MessagingException e) {
            log.warn("Email could not be sent to user '{}'", to, e);
        }
    }
}
