package com.rui.basic.app.basic.domain.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StringToNumberConverter implements AttributeConverter<String, Long> {

    @Override
    public Long convertToDatabaseColumn(String attribute) {
        // Al guardar en la BD: convierte String a Long
        if (attribute == null) {
            return null;
        }
        try {
            return Long.valueOf(attribute);
        } catch (NumberFormatException e) {
            // Si el string no es un número, intenta encontrar el ID en base al valor
            // Este es un punto donde podrías implementar alguna lógica de lookup
            // Por ejemplo, buscar en una caché o hacer una consulta a la BD
            return null; // Implementa según necesites
        }
    }

    @Override
    public String convertToEntityAttribute(Long dbData) {
        // Al leer de la BD: convierte Long a String
        if (dbData == null) {
            return null;
        }
        return dbData.toString();
    }
}