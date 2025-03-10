package com.rui.basic.app.basic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rui.basic.app.basic.domain.entities.RuiGenerics;
import com.rui.basic.app.basic.domain.entities.RuiPerson;
import com.rui.basic.app.basic.domain.entities.RuiRoles;
import com.rui.basic.app.basic.domain.entities.RuiUser;
import com.rui.basic.app.basic.exception.BusinessException;
import com.rui.basic.app.basic.exception.ResourceNotFoundException;
import com.rui.basic.app.basic.exception.UserAlreadyExistsException;
import com.rui.basic.app.basic.repository.RuiGenericsRepository;
import com.rui.basic.app.basic.repository.RuiPersonRepository;
import com.rui.basic.app.basic.repository.RuiRolesRepository;
import com.rui.basic.app.basic.repository.RuiUserRepository;
import com.rui.basic.app.basic.service.email.EmailService;
import com.rui.basic.app.basic.util.TokenUtil;
import com.rui.basic.app.basic.web.dto.RegistrationDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final RuiUserRepository userRepository;
    private final RuiPersonRepository personRepository;
    private final RuiRolesRepository rolesRepository;
    private final RuiGenericsRepository genericsRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditService auditService;
    private final TokenUtil tokenUtil;

    @Transactional
    public RuiUser registerUser(RegistrationDto registrationDto) {
        // Verificar si ya existe un usuario con ese email
        if (userRepository.existsByUsername(registrationDto.getEmail().toLowerCase())) {
            throw new UserAlreadyExistsException("Ya existe un usuario con el correo " + registrationDto.getEmail());
        }

        // Verificar si ya existe un usuario con ese número de documento
        if (userRepository.findByPersonDocumentNumber(registrationDto.getDocumentNumber()).isPresent()) {
            throw new UserAlreadyExistsException("Ya existe un usuario registrado con el documento " + registrationDto.getDocumentNumber());
        }

        try {
            // Log para debugging
            log.debug("Registrando usuario con datos: {}", registrationDto);

            // Crear persona
            RuiPerson person = createPerson(registrationDto);
            log.debug("Persona creada: {}", person);
            
            // Crear usuario
            RuiUser user = createUser(registrationDto, person);
            
            // Enviar correo de confirmación
            emailService.sendRegistrationConfirmationEmail(user);
            
            // Registrar auditoría
            auditService.createTransaction("CREATE", "CREACIÓN DE PERSONA", "RUI_PERSONS", person.getId(), null);
            auditService.createTransaction("CREATE", "CREACIÓN DE USUARIO", "RUI_USERS", user.getId(), null);
            
            return user;
        } catch (Exception e) {
            log.error("Error registrando usuario. Datos: {}, Error: {}", registrationDto, e.getMessage(), e);
            throw new BusinessException("Error en el registro de usuario: " + e.getMessage(), e);
        }
    }

    private RuiPerson createPerson(RegistrationDto dto) {
        RuiPerson person = new RuiPerson();

        // Buscar el tipo de documento como entidad
        RuiGenerics documentTypeEntity = genericsRepository.findById(Long.valueOf(dto.getDocumentType()))
        .orElseThrow(() -> new ResourceNotFoundException("Tipo de documento no encontrado"));
        
        // Asignar el ID como string - el conversor lo transformará a Long al guardar en la BD
        person.setDocumentType(documentTypeEntity.getId().toString());
        person.setDocumentNumber(dto.getDocumentNumber());
        person.setFirstName(dto.getFirstName());
        person.setSecondName(dto.getSecondName());
        person.setFirstSurname(dto.getFirstSurname());
        person.setSecondSurname(dto.getSecondSurname());
        person.setAddress(dto.getAddress());
        person.setEmail(dto.getEmail().toLowerCase());
        person.setPhone(dto.getPhone());
        person.setCellphone(dto.getCellphone());
        person.setStatus(1); // Activo
        
        return personRepository.save(person);
    }

    private RuiUser createUser(RegistrationDto dto, RuiPerson person) { 
        RuiUser user = new RuiUser();
        
        // Buscar el rol de intermediario (ID 2)
        RuiRoles intermediaryRole = rolesRepository.findById(2L)
            .orElseThrow(() -> new ResourceNotFoundException("Rol de intermediario no encontrado"));
        
        // Buscar el tipo de intermediario
        RuiGenerics intermediaryType = genericsRepository.findById(Long.valueOf(dto.getIntermediaryType()))
            .orElseThrow(() -> new ResourceNotFoundException("Tipo de intermediario no encontrado"));
        
        user.setUsername(dto.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(0); // 0 = pendiente de activación
        user.setRoleId(intermediaryRole);
        user.setPerson(person);
        // Configurar tipo de intermediario si es necesario
        
        return userRepository.save(user);
    }

    @Transactional
public void confirmAccount(String token) {
    log.debug("Procesando token de confirmación: {}", token);
    
    try {
        // Decodificar el token para obtener el ID de usuario
        Long userId = tokenUtil.getUserIdFromToken(token);
        log.debug("ID de usuario extraído del token: {}", userId);
        
        RuiUser user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("Usuario no encontrado con ID: {}", userId);
                return new ResourceNotFoundException("Usuario no encontrado");
            });
        
        // Activar el usuario
        log.debug("Activando usuario con ID {} y nombre {}", user.getId(), user.getUsername());
        user.setStatus(1); // 1 = activo
        userRepository.save(user);
        
        // Registrar auditoría
        auditService.createTransaction("UPDATE", "CONFIRMACIÓN DE USUARIO", "RUI_USERS", user.getId(), null);
        log.info("Usuario {} activado exitosamente", user.getUsername());
    } catch (Exception e) {
        log.error("Error al confirmar la cuenta con token {}: {}", token, e.getMessage(), e);
        throw e;
    }
}

    @Transactional
    public void sendPasswordRecoveryEmail(String email) {
        RuiUser user = userRepository.findByUsername(email.toLowerCase())
            .orElseThrow(() -> new ResourceNotFoundException("El usuario con correo " + email + " no existe en el sistema"));
        
        // Generar token de recuperación
        String token = tokenUtil.generatePasswordResetToken(user.getId());
        
        // Enviar correo de recuperación
        emailService.sendPasswordRecoveryEmail(email, "/auth/reset-password?token=" + token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Decodificar el token para obtener el ID de usuario
        Long userId = tokenUtil.getUserIdFromPasswordResetToken(token);
        
        RuiUser user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        // Cambiar la contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Registrar auditoría
        auditService.createTransaction("UPDATE", "CAMBIO CONTRASEÑA", "RUI_USERS", user.getId(), null);
    }
}