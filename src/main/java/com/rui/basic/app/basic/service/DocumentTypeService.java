package com.rui.basic.app.basic.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.rui.basic.app.basic.domain.entities.RuiGenerics;
import com.rui.basic.app.basic.repository.RuiGenericsRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentTypeService {

    private final RuiGenericsRepository genericsRepository;
    
    // El ID padre para tipos de documento es 5 según tus datos
    private static final Long DOCUMENT_TYPE_FATHER_ID = 5L;

    public List<RuiGenerics> getAllDocumentTypes() {
        // Buscar todos los tipos de documento que son hijos del padre DOCUMENT_TYPE_FATHER_ID
        // y que están activos (status = 1)
        return genericsRepository.findByFatherIdAndStatus(DOCUMENT_TYPE_FATHER_ID, 1);
    }
}