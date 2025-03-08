package com.rui.basic.app.basic.config;

import com.rui.basic.app.basic.domain.entities.RuiGenerics;
import com.rui.basic.app.basic.repository.RuiGenericsRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;
import java.util.Optional;

@Configuration
public class MailConfig {
    
    private static final Logger log = LoggerFactory.getLogger(MailConfig.class);
    
    private final RuiGenericsRepository ruiGenericsRepository;
    
    @Value("${spring.mail.host:#{null}}")
    private String configuredHost;
    
    @Value("${spring.mail.username:#{null}}")
    private String configuredUsername;
    
    @Value("${spring.mail.password:#{null}}")
    private String configuredPassword;
    
    @Value("${spring.mail.port:587}")
    private int configuredPort;
    
    @Value("${app.mail.use-legacy-config:false}")
    private boolean useLegacyConfig;
    
    public MailConfig(RuiGenericsRepository ruiGenericsRepository) {
        this.ruiGenericsRepository = ruiGenericsRepository;
    }
    
    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.mail.use-legacy-config", havingValue = "true")
    public JavaMailSender legacyJavaMailSender() {
        log.info("Configurando JavaMailSender con valores de la base de datos legada");
        
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        try {
            // Obtener y loggear detalladamente la configuración de la BD
            Optional<RuiGenerics> emailConfig = ruiGenericsRepository.findByIdAndStatus(19L, 1);
            Optional<RuiGenerics> passwordConfig = ruiGenericsRepository.findByIdAndStatus(21L, 1);
            Optional<RuiGenerics> hostConfig = ruiGenericsRepository.findByIdAndStatus(23L, 1);
            Optional<RuiGenerics> portConfig = ruiGenericsRepository.findByIdAndStatus(25L, 1);
            
            log.info("Valores encontrados en BD:");
            log.info("ID 19 (correo): {}", emailConfig.isPresent() ? emailConfig.get().getValue() : "NO ENCONTRADO");
            log.info("ID 21 (contraseña): {}", passwordConfig.isPresent() ? 
                    (passwordConfig.get().getValue() != null && !passwordConfig.get().getValue().isEmpty() ? "PRESENTE" : "VACÍA") : 
                    "NO ENCONTRADO");
            log.info("ID 23 (servidor): {}", hostConfig.isPresent() ? hostConfig.get().getValue() : "NO ENCONTRADO");
            log.info("ID 25 (puerto): {}", portConfig.isPresent() ? portConfig.get().getValue() : "NO ENCONTRADO");
            
            // Determinar qué valores usar (BD o properties)
            String miCorreo = emailConfig.map(RuiGenerics::getValue).orElse(configuredUsername);
            String miContrasena = passwordConfig.map(RuiGenerics::getValue).orElse(configuredPassword);
            String servidorSMTP = hostConfig.map(RuiGenerics::getValue).orElse(configuredHost);
            String puertoEnvio = portConfig.map(RuiGenerics::getValue).orElse(String.valueOf(configuredPort));
            
            // Configurar el JavaMailSender
            mailSender.setHost(servidorSMTP);
            mailSender.setPort(Integer.parseInt(puertoEnvio));
            mailSender.setUsername(miCorreo);
            
            // Solución híbrida: Si la contraseña en BD está vacía, usar la de properties
            if (miContrasena == null || miContrasena.isEmpty()) {
                log.info("Contraseña vacía en BD, usando contraseña del archivo properties");
                mailSender.setPassword(configuredPassword);
            } else {
                mailSender.setPassword(miContrasena);
            }
            
            // Si el servidor es el de la configuración antigua (172.20.22.177),
            // podría no ser accesible desde el nuevo entorno
            /*if ("172.20.22.177".equals(servidorSMTP)) {
                log.warn("Detectado servidor interno antiguo que podría no ser accesible. "+
                         "Considera cambiar app.mail.use-legacy-config=false o actualizar la BD");
            }*/
            
            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.debug", "true"); // Para ver logs detallados del envío
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");
            props.put("mail.smtp.writetimeout", "5000");
            
            log.info("JavaMailSender configurado con: host='{}', puerto='{}', usuario='{}', tieneContraseña={}",
                mailSender.getHost(), mailSender.getPort(), mailSender.getUsername(), 
                mailSender.getPassword() != null && !mailSender.getPassword().isEmpty());
            
        } catch (Exception e) {
            log.error("Error al cargar configuración de correo desde BD. Usando valores por defecto.", e);
            
            // Valores por defecto (de properties)
            mailSender.setHost(configuredHost);
            mailSender.setPort(configuredPort);
            mailSender.setUsername(configuredUsername);
            mailSender.setPassword(configuredPassword);
            
            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.debug", "true");
        }
        
        return mailSender;
    }
    
    @Bean
    @ConditionalOnProperty(name = "app.mail.use-legacy-config", havingValue = "false", matchIfMissing = true)
    public JavaMailSender defaultJavaMailSender() {
        log.info("Configurando JavaMailSender con valores del archivo properties");
        
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(configuredHost);
        mailSender.setPort(configuredPort);
        mailSender.setUsername(configuredUsername);
        mailSender.setPassword(configuredPassword);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        
        log.info("JavaMailSender properties configurado con: host='{}', puerto='{}', usuario='{}', tieneContraseña={}",
            configuredHost, configuredPort, configuredUsername, 
            configuredPassword != null && !configuredPassword.isEmpty());
        
        return mailSender;
    }
    
    // Solución alternativa híbrida: usar asuntos de la BD pero configuración SMTP de properties
    @Bean
    @ConditionalOnProperty(name = "app.mail.hybrid-config", havingValue = "true")
    public JavaMailSender hybridJavaMailSender() {
        log.info("Configurando JavaMailSender HÍBRIDO (asuntos de BD, servidor de properties)");
        
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        // Usar la configuración del archivo properties que sabemos que funciona
        mailSender.setHost(configuredHost);
        mailSender.setPort(configuredPort);
        mailSender.setUsername(configuredUsername);
        mailSender.setPassword(configuredPassword);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        
        log.info("Configuración HÍBRIDA: servidor={}, puerto={}, usuario={}",
                configuredHost, configuredPort, configuredUsername);
        
        return mailSender;
    }
}