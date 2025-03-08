package com.rui.basic.app.basic.config;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class CodingAES {
    // Usa una clave más segura (pero manteniendo compatibilidad con el código existente)
    private static final String KEY = "Rui12345Rui12345"; // Esto debería cambiarse en producción
    private static final SecretKey SECRET_KEY = new SecretKeySpec(
            KEY.getBytes(StandardCharsets.UTF_8), "AES");
    private static final int GCM_TAG_LENGTH = 128; // bits

    // Constructor privado para prevenir instanciación
    private CodingAES() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Método compatible con el código antiguo para mantener la funcionalidad existente.
     */
    public static String encrypt(String value) {
        try {
            if (value == null) return null;
            
            // Genera un IV aleatorio cada vez que se cifra
            byte[] iv = new byte[12]; // 12 bytes recomendados para GCM
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            // Usa GCM en lugar de CBC (más seguro y no requiere padding)
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, parameterSpec);
            
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            
            // Concatena el IV con el texto cifrado para poder descifrar después
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error encrypting value", e);
        }
    }

    /**
     * Método compatible con el cifrado actualizado.
     */
    public static String decrypt(String encrypted) {
        try {
            if (encrypted == null) return null;
            
            // Decodifica el Base64
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            
            // Extrae el IV (primeros 12 bytes)
            byte[] iv = new byte[12];
            byte[] cipherText = new byte[decoded.length - 12];
            
            System.arraycopy(decoded, 0, iv, 0, iv.length);
            System.arraycopy(decoded, iv.length, cipherText, 0, cipherText.length);
            
            // Usa el mismo modo GCM para descifrar
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, parameterSpec);
            
            byte[] original = cipher.doFinal(cipherText);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Si hay un error al descifrar con GCM, intenta descifrar con el método antiguo (CBC)
            // para mantener compatibilidad con datos cifrados anteriormente
            return legacyDecrypt(encrypted);
        }
    }
    
    /**
     * Método de descifrado con el algoritmo antiguo para compatibilidad con datos existentes.
     */
    private static String legacyDecrypt(String encrypted) {
        try {
            if (encrypted == null) return null;
            
            final String INIT_VECTOR = "RandomInitVector"; // Solo para compatibilidad con datos antiguos
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
            
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, iv);
            
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error decrypting value", e);
        }
    }
}