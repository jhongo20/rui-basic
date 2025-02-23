package com.rui.basic.app.basic.domain.converters;

import com.rui.basic.app.basic.domain.enums.IntermediaryState;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class IntermediaryStateConverter implements AttributeConverter<IntermediaryState, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(IntermediaryState state) {
        return state != null ? state.getState() : null;
    }
    
    @Override
    public IntermediaryState convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return IntermediaryState.fromState(dbData);
    }
}