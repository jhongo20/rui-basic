package com.rui.basic.app.basic.service;

import com.rui.basic.app.basic.domain.entities.RuiIdoniedad;
import com.rui.basic.app.basic.repository.RuiIdoniedadRepository;
import com.rui.basic.app.basic.repository.RuiSupportRepository;
import com.rui.basic.app.basic.web.dto.IdoneidadProfesionalDTO;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IdoneidadProfesionalService {

    private final RuiIdoniedadRepository idoniedadRepository;
    private final RuiSupportRepository supportRepository;  // Agregar esto

    public IdoneidadProfesionalService(
            RuiIdoniedadRepository idoniedadRepository,
            RuiSupportRepository supportRepository) {  // Agregar esto
        this.idoniedadRepository = idoniedadRepository;
        this.supportRepository = supportRepository;   // Agregar esto
    }

    public List<IdoneidadProfesionalDTO> findByIntermediary(Long intermediaryId) {
        if (intermediaryId == null) {
            return new ArrayList<>();
        }
        
        List<RuiIdoniedad> idoniedades = idoniedadRepository.findByIntermediaryId_Id(intermediaryId);
        
        if (idoniedades == null || idoniedades.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Agrupar por ID de persona para manejar múltiples registros por persona
        Map<Long, List<RuiIdoniedad>> personaMap = idoniedades.stream()
            .filter(Objects::nonNull)
            .filter(i -> i.getPersonId() != null)
            .collect(Collectors.groupingBy(i -> i.getPersonId().getId()));
        
        // Para cada persona, seleccionar el registro con la fecha de curso más reciente
        List<RuiIdoniedad> masRecientes = new ArrayList<>();
        
        personaMap.forEach((personaId, lista) -> {
            RuiIdoniedad masReciente = lista.stream()
                .filter(i -> i.getDateCourse() != null)
                .max(Comparator.comparing(RuiIdoniedad::getDateCourse))
                .orElse(lista.get(0)); // Si no hay fechas, tomar el primero
            
            masRecientes.add(masReciente);
        });
        
        // Convertir a DTOs y ordenar por fecha de curso (más reciente primero)
        return masRecientes.stream()
            .map(this::convertToDTO)
            .sorted(Comparator.comparing(IdoneidadProfesionalDTO::getDateCourse, 
                   Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList());
    }

    // Nuevo método para obtener solo los registros más recientes
    public List<IdoneidadProfesionalDTO> findMostRecentByIntermediary(Long intermediaryId) {
        log.info("Buscando registros de idoneidad más recientes para intermediario: {}", intermediaryId);
        
        List<RuiIdoniedad> masRecientes = idoniedadRepository.findMostRecentByIntermediaryId(intermediaryId);
        
        log.info("Encontrados {} registros de idoneidad", masRecientes.size());
        masRecientes.forEach(i -> {
            log.debug("Idoneidad ID: {}, Persona ID: {}, Fecha: {}, Estado Idoneidad: {}, Estado Persona: {}", 
                i.getId(), 
                i.getPersonId() != null ? i.getPersonId().getId() : null,
                i.getDateCourse(),
                i.getStatus(),
                i.getPersonId() != null ? i.getPersonId().getStatus() : null);
        });
        
        // Filtrar solo registros activos y ordenar por fecha más reciente
        Optional<RuiIdoniedad> registroMasReciente = masRecientes.stream()
            .filter(i -> i != null && 
                    i.getDateCourse() != null && 
                    i.getStatus() == 1 && 
                    i.getPersonId() != null && 
                    i.getPersonId().isActive())
            .max(Comparator.comparing(RuiIdoniedad::getDateCourse));
        
        List<IdoneidadProfesionalDTO> resultado = new ArrayList<>();
        registroMasReciente.ifPresent(idoniedad -> resultado.add(convertToDTO(idoniedad)));
        
        log.info("Se seleccionó el registro con fecha más reciente: {}", 
            registroMasReciente.map(i -> "ID: " + i.getId() + ", Fecha: " + i.getDateCourse()).orElse("Ninguno"));
        
        return resultado;
    }

    private IdoneidadProfesionalDTO convertToDTO(RuiIdoniedad idoniedad) {
        if (idoniedad == null) {
            return null;
        }
        
        IdoneidadProfesionalDTO dto = new IdoneidadProfesionalDTO();
        dto.setId(idoniedad.getId());
        
        if (idoniedad.getPersonId() != null) {
            dto.setDocumentType(idoniedad.getPersonId().getDocumentType());
            dto.setDocumentNumber(idoniedad.getPersonId().getDocumentNumber());
            dto.setFirstName(idoniedad.getPersonId().getFirstName());
            dto.setSecondName(idoniedad.getPersonId().getSecondName());
            dto.setFirstSurname(idoniedad.getPersonId().getFirstSurname());
            dto.setSecondSurname(idoniedad.getPersonId().getSecondSurname());
        }
        
        dto.setCourseEntity(idoniedad.getCourseEntity());
        dto.setDateCourse(idoniedad.getDateCourse());
        
        // Asumiendo que si el status es 1, el soporte está cargado
        //dto.setSupportUpload(idoniedad.getStatus() != null && idoniedad.getStatus() == 1);
        boolean hasSupport = supportRepository.findByIdoniedadId(idoniedad.getId()).isPresent();
        dto.setSupportUpload(hasSupport);

        // Agregar log para depuración
        log.debug("Idoneidad ID: {}, tiene soporte: {}", idoniedad.getId(), hasSupport);
        
        // Inicialmente todos están marcados como revisados
        dto.setChecked(true);
        
        return dto;
    }
}