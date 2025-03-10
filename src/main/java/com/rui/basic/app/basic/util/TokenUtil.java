package com.rui.basic.app.basic.util;

import org.springframework.stereotype.Component;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import com.rui.basic.app.basic.exception.BusinessException;

@Component
public class TokenUtil {

    private static final Map<String, Long> tokens = new HashMap<>();
    
    // Método simple para generar token de confirmación
    public String generateConfirmationToken(Long userId) {
        String token = Base64.getEncoder().encodeToString(("confirmation-" + userId + "-" + System.currentTimeMillis()).getBytes());
        tokens.put(token, userId);
        return token;
    }
    
    // Método simple para generar token de reset de contraseña
    public String generatePasswordResetToken(Long userId) {
        String token = Base64.getEncoder().encodeToString(("reset-" + userId + "-" + System.currentTimeMillis()).getBytes());
        tokens.put(token, userId);
        return token;
    }
    
    // Método para obtener ID de usuario de token
    public Long getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new BusinessException("Token inválido o vacío");
        }
        
        try {
            // Primero intenta recuperarlo del mapa en memoria
            if (tokens.containsKey(token)) {
                return tokens.get(token);
            }
            
            // Si no está en el mapa, trata de extraerlo del formato "id-xxx-timestamp"
            String[] parts = token.split("-");
            if (parts.length >= 1) {
                return Long.parseLong(parts[0]);
            }
            
            throw new BusinessException("Formato de token inválido");
        } catch (NumberFormatException e) {
            throw new BusinessException("No se pudo extraer el ID de usuario del token", e);
        }
    }
    
    // Método para obtener ID de usuario de token de reset de contraseña
    public Long getUserIdFromPasswordResetToken(String token) {
        return getUserIdFromToken(token);
    }
}