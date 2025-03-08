package com.rui.basic.app.basic.config;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Clase que implementa el método original de cifrado para asegurar
 * la compatibilidad con contraseñas existentes.
 */
class CodingAESLegacy {
    private static final String KEY = "Rui12345Rui12345";
    private static final String INIT_VECTOR = "RandomInitVector";

    public static String encrypt(String value) {
        try {
            if (value == null) return null;
            
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
            
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error encrypting value", e);
        }
    }
}