package com.rui.basic.app.basic.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.rui.basic.app.basic.domain.entities.RuiGenerics;
import com.rui.basic.app.basic.repository.RuiGenericsRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IntermediaryTypeService {

    private final RuiGenericsRepository genericsRepository;
    
    // Asumo que el ID padre para tipos de intermediario es diferente
    // Puedes ajustar este valor según tus datos reales
    private static final Long INTERMEDIARY_TYPE_FATHER_ID = 1L; // Cambia este valor al correcto

    public List<RuiGenerics> getAllIntermediaryTypes() {
        // Buscar todos los tipos de intermediario que son hijos del padre INTERMEDIARY_TYPE_FATHER_ID
        // y que están activos (status = 1)
        return genericsRepository.findByFatherIdAndStatus(INTERMEDIARY_TYPE_FATHER_ID, 1);
    }
}