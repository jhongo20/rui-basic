package com.rui.basic.app.basic.web.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class RegistrationDto {

    @NotBlank(message = "Se debe seleccionar un Tipo de documento")
    private String documentType;
    
    @NotBlank(message = "El número de documento es requerido")
    @Size(min = 3, max = 15, message = "Ingrese número de identificación entre 3 a 15 caracteres")
    private String documentNumber;
    
    @NotBlank(message = "El primer nombre es requerido")
    @Size(max = 25, message = "El primer nombre debe tener como máximo 25 caracteres")
    private String firstName;
    
    @Size(max = 25, message = "El segundo nombre debe tener como máximo 25 caracteres")
    private String secondName;
    
    @NotBlank(message = "El primer apellido es requerido")
    @Size(max = 25, message = "El primer apellido debe tener como máximo 25 caracteres")
    private String firstSurname;
    
    @Size(max = 25, message = "El segundo apellido debe tener como máximo 25 caracteres")
    private String secondSurname;
    
    @NotBlank(message = "La dirección es requerida")
    @Size(max = 50, message = "La dirección debe tener máximo 50 caracteres")
    private String address;
    
    @NotBlank(message = "El correo electrónico es requerido")
    @Size(max = 320, message = "El correo electrónico debe tener como máximo 320 caracteres")
    @Email(message = "Ingrese un correo electrónico válido")
    private String email;
    
    @NotBlank(message = "Debe confirmar el correo electrónico")
    @Email(message = "Ingrese un correo electrónico válido")
    private String confirmEmail;
    
    @Pattern(regexp = "[0-9]*", message = "El teléfono fijo debe ser un valor numérico")
    @Size(min = 7, max = 7, message = "El teléfono fijo debe tener 7 dígitos")
    private String phone;
    
    @NotBlank(message = "El teléfono celular es requerido")
    @Pattern(regexp = "[0-9]*", message = "El teléfono celular debe ser un valor numérico")
    @Size(min = 10, max = 10, message = "El teléfono celular debe tener 10 dígitos")
    private String cellphone;
    
    @NotBlank(message = "Se debe seleccionar un Tipo de intermediario")
    private String intermediaryType;
    
    @NotBlank(message = "La contraseña es requerida")
    private String password;
    
    @NotBlank(message = "Debe confirmar la contraseña")
    private String confirmPassword;
}