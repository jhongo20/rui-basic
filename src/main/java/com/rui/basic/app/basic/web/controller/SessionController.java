package com.rui.basic.app.basic.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    @PostMapping("/extend")
    public ResponseEntity<Map<String, Object>> extendSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, Object> response = new HashMap<>();
        
        if (session != null) {
            // Establecer el último tiempo de acceso para extender la sesión
            session.setMaxInactiveInterval(600); // 10 minutos
            response.put("success", true);
            response.put("message", "Sesión extendida exitosamente");
            response.put("expiresIn", session.getMaxInactiveInterval());
        } else {
            response.put("success", false);
            response.put("message", "No se pudo extender la sesión");
        }
        
        return ResponseEntity.ok(response);
    }
}
