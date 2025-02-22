package com.rui.basic.app.basic.config;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class LegacyPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        try {
            return CodingAES.encrypt(rawPassword.toString());
        } catch (Exception e) {
            throw new RuntimeException("Error encoding password", e);
        }
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        try {
            String encryptedInput = CodingAES.encrypt(rawPassword.toString());
            return encryptedInput.equals(encodedPassword);
        } catch (Exception e) {
            throw new RuntimeException("Error matching passwords", e);
        }
    }
}