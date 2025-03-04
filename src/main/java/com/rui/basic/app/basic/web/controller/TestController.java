package com.rui.basic.app.basic.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rui.basic.app.basic.domain.entities.RuiUser;
import com.rui.basic.app.basic.repository.RuiUserRepository;
import com.rui.basic.app.basic.service.email.EmailService;
import com.rui.basic.app.basic.service.email.LegacyEmailSubjectService;
import com.rui.basic.app.basic.web.dto.EmailTemplateDTO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {
    
    @Autowired
    private RuiUserRepository userRepository;
    
    @Autowired
    private EntityManager entityManager;

    private final EmailService emailService;
    private final LegacyEmailSubjectService legacyEmailSubjectService;

    public TestController(EmailService emailService, LegacyEmailSubjectService legacyEmailSubjectService) {
        this.emailService = emailService;
        this.legacyEmailSubjectService = legacyEmailSubjectService;
    }
    
    @GetMapping("/check-user/{username}")
    public ResponseEntity<?> checkUser(@PathVariable String username) {
        log.debug("Verificando usuario: {}", username);
        
        Map<String, Object> response = new HashMap<>();
        
        // Consulta nativa para debug
        Query nativeQuery = entityManager.createNativeQuery(
            "SELECT id, username, status FROM rui_users WHERE LOWER(username) = LOWER(:username)", 
            RuiUser.class);
        nativeQuery.setParameter("username", username);
        
        List<?> results = nativeQuery.getResultList();
        log.debug("Resultados de consulta nativa: {}", results.size());
        
        // Consulta con repository
        Optional<RuiUser> user = userRepository.findByUsername(username);
        
        if (user.isPresent()) {
            RuiUser foundUser = user.get();
            response.put("exists", true);
            response.put("status", foundUser.getStatus());
            response.put("id", foundUser.getId());
            response.put("username", foundUser.getUsername());
            log.debug("Usuario encontrado: {}", foundUser);
        } else {
            response.put("exists", false);
            log.debug("Usuario no encontrado");
            
            // Listar todos los usuarios para debug
            List<RuiUser> allUsers = userRepository.findAll();
            log.debug("Total usuarios en BD: {}", allUsers.size());
            allUsers.forEach(u -> log.debug("Usuario en BD: {}", u.getUsername()));
        }
        
        return ResponseEntity.ok(response);
    }


    @PostMapping("/email")
public ResponseEntity<String> testLegacyEmail(
        @RequestParam String to,
        @RequestParam(defaultValue = "1") int subjectType) {
    
    try {
        // Obtener el asunto desde la BD
        String subject = legacyEmailSubjectService.getSubjectById(subjectType);
        
        // Crear un DTO de email con una plantilla simple
        EmailTemplateDTO emailDTO = new EmailTemplateDTO();
        emailDTO.setTo(to);
        emailDTO.setSubject(subject);
        
        // Usando una plantilla de prueba genérica
        emailDTO.setTemplate("email/test-template");
        
        // Variables para la plantilla - usando Map<String, String>
        Map<String, String> variables = new HashMap<>();
        variables.put("message", "Este es un correo de prueba usando la configuración legada.");
        variables.put("subjectType", String.valueOf(subjectType));
        emailDTO.setTemplateVariables(variables);
        
        // Enviar el correo
        emailService.sendEmail(emailDTO);
        
        return ResponseEntity.ok("Correo enviado correctamente a " + to + " con asunto tipo " + subjectType);
        
    } catch (Exception e) {
        return ResponseEntity.internalServerError()
            .body("Error al enviar correo: " + e.getMessage());
    }
}

    @GetMapping("/email-form")
    public String showEmailTestForm(Model model) {
        model.addAttribute("emailTest", new EmailTestForm());
        return "email-test-form";
    }
    
    @Data
    public static class EmailTestForm {
        private String to;
        private int subjectType = 1;
    }

}