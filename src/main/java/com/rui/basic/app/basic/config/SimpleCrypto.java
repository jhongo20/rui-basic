package com.rui.basic.app.basic.config;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleCrypto {
    private static final Logger logger = Logger.getLogger(SimpleCrypto.class.getName());
    // Tamaño de etiqueta de autenticación GCM (en bits)
    private static final int GCM_TAG_LENGTH = 128;
    // Tamaño del IV para GCM (en bytes)
    private static final int GCM_IV_LENGTH = 12;
    
    // La clave debe ser manejada de forma segura (idealmente en un almacén de claves)
    // Esta es una implementación temporal para desarrollo
    private static SecretKey getSecretKey() {
        try {
            // En producción, esta clave debería almacenarse de forma segura
            // y no debería ser generada cada vez
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // AES-256
            return keyGen.generateKey();
            
            // Alternativa: cargar clave desde un almacén seguro
            // return loadKeyFromSecureStorage();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error al obtener la clave secreta", ex);
            throw new RuntimeException("Error de encriptación", ex);
        }
    }
    
    public static String encrypt(String value) {
        try {
            // Genera un IV aleatorio
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            // Obtiene clave secreta
            SecretKey key = getSecretKey();
            
            // Inicializa el cifrado con AES en modo GCM
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
            
            // Cifra los datos
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            
            // Concatena IV y datos cifrados
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
            byteBuffer.put(iv);
            byteBuffer.put(encrypted);
            
            // Codifica en Base64
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error al encriptar datos", ex);
            throw new RuntimeException("Error de encriptación", ex);
        }
    }

    public static String decrypt(String encrypted) {
        try {
            // Decodifica de Base64
            byte[] encryptedData = Base64.getDecoder().decode(encrypted);
            
            // Extrae IV y datos cifrados
            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);
            
            // Obtiene clave secreta
            SecretKey key = getSecretKey();
            
            // Inicializa el cifrado para descifrar
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            
            // Descifra los datos
            byte[] original = cipher.doFinal(cipherText);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error al desencriptar datos", ex);
            throw new RuntimeException("Error de desencriptación", ex);
        }
    }
}