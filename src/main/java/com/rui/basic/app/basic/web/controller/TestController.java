package com.rui.basic.app.basic.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rui.basic.app.basic.domain.entities.RuiUser;
import com.rui.basic.app.basic.repository.RuiUserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {
    
    @Autowired
    private RuiUserRepository userRepository;
    
    @Autowired
    private EntityManager entityManager;
    
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
}