package com.rui.basic.app.basic.domain.enums;

public enum DocumentType {
    CC("1", "Cédula de Ciudadanía"),
    CE("2", "Cédula de Extranjería"),
    PA("3", "Pasaporte");

    private final String code;
    private final String description;

    DocumentType(String code, String description) {
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
