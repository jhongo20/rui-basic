package com.rui.basic.app.basic.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.rui.basic.app.basic.domain.entities.RuiUser;
import com.rui.basic.app.basic.web.dto.EmailTemplateDTO;


@Service
@RequiredArgsConstructor
public class EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.base-url}")
    private String baseUrl;

    public void sendRegistrationConfirmationEmail(RuiUser user) {
        try {
            String htmlContent = createRegistrationEmailContent(user);
            sendEmail(user.getUsername(), "Confirmación de Registro RUI", htmlContent);
            log.info("Confirmation email sent to: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Error sending confirmation email to {}", user.getUsername(), e);
            // No lanzamos la excepción para no interrumpir el flujo de registro
        }
    }

    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        emailSender.send(message);
    }

    private String createRegistrationEmailContent(RuiUser user) {
        StringBuilder html = new StringBuilder();
        html.append("<html>\n")
            .append("\t<head>\n")
            .append("\t\t<title>RUI</title>\n")
            .append("\t</head>\n")
            .append("\t<body>\n")
            .append("\t\t<table style=\"width:80%;margin:0 auto;font-family:Arial;\">\n")
            .append("\t\t\t<tr style=\"background-color:#35348A;color:#FFFF;\">\n")
            .append("\t\t\t\t<td style=\"padding:10px 0;text-align:center;font-size:16px;font-weight:bolder;\">")
            .append("SISTEMA DE REGISTRO ÚNICO DE INTERMEDIARIOS</td>\n")
            .append("\t\t\t</tr>\n")
            .append("\t\t\t<tr><td>&nbsp;</td></tr>\n")
            .append("\t\t\t<tr style=\"font-size:14px;\">\n")
            .append("\t\t\t\t<td>\n")
            .append("\t\t\t\t\t<p>Su cuenta ha sido registrada. Sus credenciales son:</p>\n")
            .append("\t\t\t\t\t<ul>\n")
            .append("\t\t\t\t\t\t<li>Usuario: ").append(user.getUsername()).append("</li>\n")
            .append("\t\t\t\t\t</ul>\n")
            .append("\t\t\t\t\t<p>Por favor, haga clic <a href='")
            .append(generateConfirmationLink(user))
            .append("'>aquí</a> para activar su cuenta.</p>\n")
            .append("\t\t\t\t</td>\n")
            .append("\t\t\t</tr>\n")
            .append("\t\t\t<tr><td>&nbsp;</td></tr>\n")
            .append("\t\t\t<tr style=\"background-color:#35348A;color:#FFFF;\">\n")
            .append("\t\t\t\t<td style=\"padding:10px 0;text-align:center;font-size:16px;font-weight:bolder;\">")
            .append("MINISTERIO DEL TRABAJO</td>\n")
            .append("\t\t\t</tr>\n")
            .append("\t\t</table>\n")
            .append("\t</body>\n")
            .append("</html>");
        
        return html.toString();
    }

    private String generateConfirmationLink(RuiUser user) {
        return baseUrl + "/auth/confirm?token=" + generateConfirmationToken(user);
    }

    private String generateConfirmationToken(RuiUser user) {
        // Implementar la generación segura del token
        // Por ahora, un ejemplo simple:
        return String.format("%d-%s-%d", 
            user.getId(), 
            user.getUsername().substring(0, 3), 
            System.currentTimeMillis());
    }

    //-----
    public void sendIntermediaryStatusEmail(String to, int statusType, String radicateNumber, 
            String name, String address, String city, String intermediaryType) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("address", address);
            context.setVariable("city", city);
            context.setVariable("radicateNumber", radicateNumber);
            context.setVariable("intermediaryType", intermediaryType);
            context.setVariable("currentDate", getCurrentFormattedDate());
            
            String template = getTemplateNameForStatus(statusType);
            String subject = getSubjectForStatus(statusType);
            
            String htmlContent = templateEngine.process(template, context);
            sendEmail(to, subject, htmlContent);
            
            log.info("Status email sent to: {} for status: {}", to, statusType);
        } catch (Exception e) {
            log.error("Error sending status email to {} for status {}", to, statusType, e);
        }
    }

    private String getTemplateNameForStatus(int statusType) {
        return switch (statusType) {
            case 1 -> "email/registration-completed.html";
            case 2 -> "email/account-confirmation.html";
            case 3 -> "email/password-recovery.html";
            case 4 -> "email/request-withdrawn.html";
            case 5 -> "email/complement-information.html";
            case 6 -> "email/request-approved.html";
            case 7 -> "email/voluntary-withdrawal.html";
            case 8 -> "email/status-change.html";
            case 9 -> "email/request-cancelled.html";
            default -> throw new IllegalArgumentException("Invalid status type: " + statusType);
        };
    }

    private String getSubjectForStatus(int statusType) {
        return switch (statusType) {
            case 1 -> "Inscripción Registro Único de Intermediarios";
            case 2 -> "Confirmación de Cuenta RUI";
            case 3 -> "Recuperación de Contraseña RUI";
            case 4 -> "Desistimiento de Solicitud RUI";
            case 5 -> "Solicitud Complementar Información RUI";
            case 6 -> "Aprobación Registro de Intermediarios";
            case 7 -> "Retiro Voluntario RUI";
            case 8 -> "Cambio de Estado RUI";
            case 9 -> "Cancelación de Solicitud RUI";
            default -> "Notificación RUI";
        };
    }

    public void sendPasswordRecoveryEmail(String to, String recoveryLink) {
        try {
            Context context = new Context();
            context.setVariable("recoveryLink", recoveryLink);
            context.setVariable("currentDate", getCurrentFormattedDate());
            
            String htmlContent = templateEngine.process("email/password-recovery.html", context);
            sendEmail(to, "Recuperación de Contraseña RUI", htmlContent);
            
            log.info("Password recovery email sent to: {}", to);
        } catch (Exception e) {
            log.error("Error sending password recovery email to {}", to, e);
        }
    }

    private String getCurrentFormattedDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(new java.util.Date());
    }

    public void sendEmail(EmailTemplateDTO emailDTO) throws MessagingException {
        Context context = new Context();
        emailDTO.getTemplateVariables().forEach(context::setVariable);
        
        String htmlContent = templateEngine.process(emailDTO.getTemplate(), context);
        sendEmail(emailDTO.getTo(), emailDTO.getSubject(), htmlContent);
    }
}