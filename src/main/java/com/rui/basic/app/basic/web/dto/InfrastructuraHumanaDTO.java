package com.rui.basic.app.basic.web.dto;

import lombok.Data;

@Data
public class InfrastructuraHumanaDTO {
    private Long id;
    // Información del profesional
    private String documentType;
    private String documentNumber;
    private String firstName;
    private String secondName;
    private String firstSurname;
    private String secondSurname;
    private Boolean checked; // Para el estado del checkbox
}
