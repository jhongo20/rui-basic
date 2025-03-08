package com.rui.basic.app.basic.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Objects;

@Component
public class LegacyPasswordEncoder implements PasswordEncoder {
    
    // Codificador BCrypt para nuevas contraseñas
    private final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
    
    // Prefijo para identificar contraseñas BCrypt
    private static final String BCRYPT_PREFIX = "{bcrypt}";
    
    // Constantes para el método AES anterior (solo para compatibilidad con contraseñas existentes)
    private static final String LEGACY_KEY = "Rui12345Rui12345";
    private static final String LEGACY_IV = "RandomInitVector";

    @Override
    public String encode(CharSequence rawPassword) {
        // Para nuevas contraseñas, usamos BCrypt (más seguro)
        return BCRYPT_PREFIX + bcryptEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        
        // Verifica si la contraseña es una nueva contraseña BCrypt
        if (encodedPassword.startsWith(BCRYPT_PREFIX)) {
            String bcryptPart = encodedPassword.substring(BCRYPT_PREFIX.length());
            return bcryptEncoder.matches(rawPassword, bcryptPart);
        } 
        
        // Para compatibilidad hacia atrás con contraseñas antiguas (AES)
        try {
            String encryptedLegacy = legacyEncrypt(rawPassword.toString());
            // Usa Objects.equals para una comparación segura contra null
            return Objects.equals(encryptedLegacy, encodedPassword);
        } catch (Exception e) {
            // Si hay un error, la verificación falla
            return false;
        }
    }
    
    /**
     * Método interno que replica el comportamiento exacto del cifrado AES anterior.
     * Solo para compatibilidad con contraseñas existentes.
     */
    private String legacyEncrypt(String value) {
        try {
            if (value == null) return null;
            
            IvParameterSpec iv = new IvParameterSpec(LEGACY_IV.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec keySpec = new SecretKeySpec(LEGACY_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error encrypting value", e);
        }
    }

    
}