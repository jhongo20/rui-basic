package com.rui.basic.app.basic.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rui.basic.app.basic.domain.entities.RuiRoles;
import com.rui.basic.app.basic.domain.entities.RuiUser;
import com.rui.basic.app.basic.domain.enums.IntermediaryType;
import com.rui.basic.app.basic.exception.ResourceNotFoundException;
import com.rui.basic.app.basic.exception.UserAlreadyExistsException;
import com.rui.basic.app.basic.repository.RuiRolesRepository;
import com.rui.basic.app.basic.repository.RuiUserRepository;
import com.rui.basic.app.basic.service.email.EmailService;
import com.rui.basic.app.basic.web.dto.RoleCountDto;
import com.rui.basic.app.basic.web.dto.UserRegistrationDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final RuiUserRepository userRepository;
    private final RuiRolesRepository ruiRolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    @Transactional
    public RuiUser registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new UserAlreadyExistsException("Ya existe un usuario con ese nombre de usuario");
        }

        RuiUser user = new RuiUser();
        user.setUsername(registrationDto.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        
        // Obtener el rol por defecto (ID 2 para Intermediario)
        RuiRoles defaultRole = ruiRolesRepository.findById(2L)  // Usar Long en lugar de BigDecimal
            .orElseThrow(() -> new RuntimeException("Rol por defecto no encontrado"));
        
        user.setRoleId(defaultRole);
        user.setStatus(1);

        return userRepository.save(user);
    }

    public List<Map<String, String>> getDocumentTypes() {
        List<Map<String, String>> documentTypes = new ArrayList<>();
        
        // Basado en el código original JSF
        documentTypes.add(createTypeMap("1", "Cédula de Ciudadanía"));
        documentTypes.add(createTypeMap("2", "Cédula de Extranjería"));
        documentTypes.add(createTypeMap("3", "Pasaporte"));
        
        return documentTypes;
    }

    private Map<String, String> createTypeMap(String id, String value) {
        Map<String, String> map = new HashMap<>();
        map.put("id", id);
        map.put("value", value);
        return map;
    }

    public List<Map<String, String>> getIntermediaryTypes() {
        return Arrays.stream(IntermediaryType.values())
            .map(type -> Map.of(
                "id", type.getCode(),
                "value", type.getDescription()
            ))
            .collect(Collectors.toList());
    }

    public boolean validatePassword(String password) {
        // Mínimo 8 caracteres
        if (password.length() < 8) return false;
        
        // Debe contener al menos una mayúscula
        if (!password.matches(".*[A-Z].*")) return false;
        
        // Debe contener al menos una minúscula
        if (!password.matches(".*[a-z].*")) return false;
        
        // Debe contener al menos un número
        if (!password.matches(".*\\d.*")) return false;
        
        // Debe contener al menos un carácter especial
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) return false;
        
        return true;
    }

    public RuiUser findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    public RuiUser findByDocumentNumber(String documentNumber) {
        return userRepository.findByPersonDocumentNumber(documentNumber)
            .orElse(null);
    }

    @Transactional
    public void updatePassword(String username, String newPassword) {
        RuiUser user = findByUsername(username);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        }
    }

    @Transactional
    public void activateUser(String username) {
        RuiUser user = findByUsername(username);
        if (user != null) {
            user.setStatus(1);
            userRepository.save(user);
        }
    }

    public boolean isPasswordValid(String username, String password) {
        RuiUser user = findByUsername(username);
        return user != null && passwordEncoder.matches(password, user.getPassword());
    }


    public long countUsers() {
        return userRepository.count();
    }
    
    public long countActiveUsers() {
        return userRepository.countByStatus(1);
    }
    
    
    public List<RuiUser> getAllUsers() {
        return userRepository.findAll();
    }
    
    public RuiUser getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }
    
    
    public Map<String, Long> getUserCountByRole() {
        List<RoleCountDto> roleCounts = userRepository.countUsersByRole();
        return roleCounts.stream()
            .collect(Collectors.toMap(
                RoleCountDto::getRoleName,
                RoleCountDto::getUserCount
            ));
    }
    
    public List<RuiUser> filterUsers(String role, Integer status) {
        if (role != null && status != null) {
            return userRepository.findByRoleIdNameAndStatus(role, status);
        } else if (role != null) {
            return userRepository.findByRoleIdName(role);
        } else if (status != null) {
            return userRepository.findByStatus(status);
        }
        return getAllUsers();
    }

}
