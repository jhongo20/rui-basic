package com.rui.basic.app.basic.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.rui.basic.app.basic.web.dto.LoginDto;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public UserDetails authenticate(LoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDto.getUsername(), 
                    loginDto.getPassword()
                )
            );
            
            return (UserDetails) authentication.getPrincipal();
        } catch (Exception e) {
            log.error("Error en la autenticación para el usuario: {}", loginDto.getUsername(), e);
            throw e;
        }
    }
}
