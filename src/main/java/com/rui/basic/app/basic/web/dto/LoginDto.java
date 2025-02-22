package com.rui.basic.app.basic.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
    @NotBlank(message = "El email es requerido")
    @Email(message = "Debe ser un email válido")
    private String username;
    
    @NotBlank(message = "La contraseña es requerida")
    private String password;
}
