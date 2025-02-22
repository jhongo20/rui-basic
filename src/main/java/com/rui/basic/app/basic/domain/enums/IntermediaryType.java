package com.rui.basic.app.basic.domain.enums;

public enum IntermediaryType {
    AGENCIA("1", "Agencia de Empleo"),
    BOLSA("2", "Bolsa de Empleo"),
    EMPRESA("3", "Empresa de Servicios Temporales");

    private final String code;
    private final String description;

    IntermediaryType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
