package com.rui.basic.app.basic.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.rui.basic.app.basic.domain.entities.RuiUser;
import com.rui.basic.app.basic.repository.RuiUserRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private RuiUserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Intentando autenticar usuario: '{}'", username);
        
        try {
            // Verificar si hay duplicados
            int userCount = userRepository.countByUsername(username);
            if (userCount > 1) {
                log.error("Usuario duplicado en la base de datos: {}", username);
                throw new AuthenticationServiceException(
                    "Error de configuración: Usuario duplicado en el sistema. Contacte al administrador.");
            }
            
            // Buscar usuario activo
            RuiUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    // Verificar si existe pero está inactivo
                    if (userCount == 1) {
                        return new DisabledException("Usuario inactivo o bloqueado");
                    }
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });

            log.debug("Usuario encontrado - ID: {}, Status: {}", user.getId(), user.getStatus());
            
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (user.getRoleId() != null) {
                String role = "ROLE_" + user.getRoleId().getName();
                authorities.add(new SimpleGrantedAuthority(role));
                log.debug("Rol asignado: {}", role);
            }

            return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(false) // Ya verificamos el estado en la consulta
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
                
        } catch (DisabledException e) {
            log.error("Usuario inactivo: {}", username);
            throw e;
        } catch (AuthenticationServiceException e) {
            log.error("Error de autenticación para usuario '{}': {}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error en autenticación para usuario '{}': {}", username, e.getMessage());
            throw new AuthenticationServiceException(e.getMessage(), e);
        }
    }
}