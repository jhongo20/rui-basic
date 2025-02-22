package com.rui.basic.app.basic.web.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRegistrationDto {
    @NotBlank(message = "El tipo de documento es requerido")
    private String documentType;

    @NotBlank(message = "El número de documento es requerido")
    @Size(max = 15, message = "El número de documento no puede exceder 15 caracteres")
    private String documentNumber;

    @NotBlank(message = "El primer nombre es requerido")
    @Size(max = 25, message = "El primer nombre no puede exceder 25 caracteres")
    private String firstName;

    @Size(max = 25, message = "El segundo nombre no puede exceder 25 caracteres")
    private String secondName;

    @NotBlank(message = "El primer apellido es requerido")
    @Size(max = 25, message = "El primer apellido no puede exceder 25 caracteres")
    private String firstSurname;

    @Size(max = 25, message = "El segundo apellido no puede exceder 25 caracteres")
    private String secondSurname;

    @NotBlank(message = "El tipo de intermediario es requerido")
    private String intermediaryType;

    @NotBlank(message = "La dirección es requerida")
    @Size(max = 50, message = "La dirección no puede exceder 50 caracteres")
    private String address;

    @Size(max = 7, message = "El teléfono fijo no puede exceder 7 caracteres")
    private String phone;

    @NotBlank(message = "El teléfono móvil es requerido")
    @Size(max = 10, message = "El teléfono móvil no puede exceder 10 caracteres")
    private String cellphone;

    @NotBlank(message = "El correo electrónico es requerido")
    @Size(max = 320, message = "El correo electrónico no puede exceder 320 caracteres")
    private String username;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, max = 25, message = "La contraseña debe tener entre 8 y 25 caracteres")
    private String password;

    @NotBlank(message = "Debe confirmar la contraseña")
    private String confirmPassword;
}
