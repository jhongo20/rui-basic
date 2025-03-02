package com.rui.basic.app.basic.service;

import com.rui.basic.app.basic.domain.entities.RuiHistoryDetails;
import com.rui.basic.app.basic.domain.entities.RuiIdoniedad;
import com.rui.basic.app.basic.domain.entities.RuiIntermediary;
import com.rui.basic.app.basic.domain.entities.RuiIntermediaryHistory;
import com.rui.basic.app.basic.repository.RuiHistoryDetailsRepository;
import com.rui.basic.app.basic.repository.RuiIdoniedadRepository;
import com.rui.basic.app.basic.repository.RuiIntermediaryHistoryRepository;
import com.rui.basic.app.basic.repository.RuiSupportRepository;
import com.rui.basic.app.basic.web.dto.FormFieldStateDTO;
import com.rui.basic.app.basic.web.dto.IdoneidadProfesionalDTO;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
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

    private final RuiHistoryDetailsRepository historyDetailsRepository;
    private final RuiIntermediaryHistoryRepository intermediaryHistoryRepository;

    public IdoneidadProfesionalService(
            RuiIdoniedadRepository idoniedadRepository,
            RuiSupportRepository supportRepository,
            RuiHistoryDetailsRepository historyDetailsRepository,
            RuiIntermediaryHistoryRepository intermediaryHistoryRepository) {
        this.idoniedadRepository = idoniedadRepository;
        this.supportRepository = supportRepository;
        this.historyDetailsRepository = historyDetailsRepository;
        this.intermediaryHistoryRepository = intermediaryHistoryRepository;
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

    /**
     * Convierte un RuiIdoniedad a IdoneidadProfesionalDTO.
     */
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

    /**
 * Obtiene el estado de la observación general para un registro de idoneidad.
 */
public FormFieldStateDTO getObservation(Long idoniedadId) {
    log.debug("Buscando observación para Idoneidad ID: {}", idoniedadId);
    Optional<RuiIdoniedad> idoniedadOptional = idoniedadRepository.findById(idoniedadId);
    
    FormFieldStateDTO state = new FormFieldStateDTO();
    
    if (idoniedadOptional.isEmpty()) {
        log.warn("Idoneidad no encontrada: {}", idoniedadId);
        state.setIconClose(false);
        state.setCommentDisabled(true);
        state.setObservation("Idoneidad no encontrada");
        return state;
    }

    RuiIdoniedad idoniedad = idoniedadOptional.get();
    
    // Verificar si el registro está activo (status = 1) y la persona asociada también
    if (idoniedad.getStatus() != 1 || idoniedad.getPersonId() == null || !idoniedad.getPersonId().isActive()) {
        log.warn("Idoneidad ID {} inactiva o persona asociada inactiva", idoniedadId);
        state.setIconClose(false);
        state.setCommentDisabled(true);
        state.setObservation("Idoneidad o persona inactiva");
        return state;
    }

    // Buscar la observación general en RuiHistoryDetails
    Optional<RuiHistoryDetails> detail = historyDetailsRepository
        .findByTableIdAndTableName(idoniedadId, "RUI_IDONIEDAD")
        .stream()
        .filter(d -> "general".equals(d.getFieldName())) // Filtrar por observación general
        .findFirst();
    
    if (detail.isPresent()) {
        state.setIconClose(true);
        state.setCommentDisabled(false);
        state.setObservation(detail.get().getObservation());
    } else {
        state.setIconClose(false);
        state.setCommentDisabled(true);
        state.setObservation("");
    }
    
    log.debug("Estado de la observación para Idoneidad ID {}: {}", idoniedadId, state);
    return state;
}

    /**
 * Crea una observación general para un registro de idoneidad.
 */
public FormFieldStateDTO createObservation(Long idoniedadId) {
    log.debug("Intentando crear observación para Idoneidad ID: {}", idoniedadId);
    Optional<RuiIdoniedad> idoniedadOptional = idoniedadRepository.findById(idoniedadId);
    
    if (idoniedadOptional.isEmpty()) {
        log.warn("Idoneidad no encontrada: {}", idoniedadId);
        FormFieldStateDTO state = new FormFieldStateDTO();
        state.setIconClose(false);
        state.setCommentDisabled(true);
        state.setObservation("Idoneidad no encontrada");
        return state;
    }

    RuiIdoniedad idoniedad = idoniedadOptional.get();
    
    // Verificar si el registro está activo
    if (idoniedad.getStatus() != 1 || idoniedad.getPersonId() == null || !idoniedad.getPersonId().isActive()) {
        log.warn("Idoneidad ID {} inactiva o persona asociada inactiva", idoniedadId);
        FormFieldStateDTO state = new FormFieldStateDTO();
        state.setIconClose(false);
        state.setCommentDisabled(true);
        state.setObservation("Idoneidad o persona inactiva");
        return state;
    }

    RuiIntermediaryHistory history = findOrCreateActiveHistory(idoniedad.getIntermediaryId());
    
    // Verificar si ya existe una observación general para evitar duplicados
    Optional<RuiHistoryDetails> existingDetail = historyDetailsRepository
        .findByTableIdAndTableName(idoniedadId, "RUI_IDONIEDAD")
        .stream()
        .filter(d -> "general".equals(d.getFieldName()))
        .findFirst();

    if (!existingDetail.isPresent()) {
        RuiHistoryDetails detail = new RuiHistoryDetails();
        detail.setIntermediaryHistoryId(history);
        detail.setFieldName("general"); // Usamos "general" para observaciones generales del registro
        detail.setObservation(""); // Inicialmente vacío
        detail.setTableName("RUI_IDONIEDAD");
        detail.setTableId(idoniedadId);
        
        historyDetailsRepository.save(detail);
        log.info("Observación general creada para Idoneidad ID: {}", idoniedadId);
    } else {
        log.warn("Ya existe una observación general para Idoneidad ID: {}", idoniedadId);
    }

    // Devolver el estado actualizado
    return getObservation(idoniedadId);
}

    /**
 * Elimina la observación general para un registro de idoneidad.
 */
public FormFieldStateDTO removeObservation(Long idoniedadId) {
    log.debug("Intentando eliminar observación para Idoneidad ID: {}", idoniedadId);
    Optional<RuiIdoniedad> idoniedadOptional = idoniedadRepository.findById(idoniedadId);
    
    if (idoniedadOptional.isEmpty()) {
        log.warn("Idoneidad no encontrada: {}", idoniedadId);
        FormFieldStateDTO state = new FormFieldStateDTO();
        state.setIconClose(false);
        state.setCommentDisabled(true);
        state.setObservation("Idoneidad no encontrada");
        return state;
    }

    RuiIdoniedad idoniedad = idoniedadOptional.get();
    
    // Verificar si el registro está activo
    if (idoniedad.getStatus() != 1 || idoniedad.getPersonId() == null || !idoniedad.getPersonId().isActive()) {
        log.warn("Idoneidad ID {} inactiva o persona asociada inactiva", idoniedadId);
        FormFieldStateDTO state = new FormFieldStateDTO();
        state.setIconClose(false);
        state.setCommentDisabled(true);
        state.setObservation("Idoneidad o persona inactiva");
        return state;
    }

    historyDetailsRepository.findByTableIdAndTableName(idoniedadId, "RUI_IDONIEDAD")
        .stream()
        .filter(d -> "general".equals(d.getFieldName())) // Solo eliminamos observaciones generales
        .findFirst()
        .ifPresent(historyDetailsRepository::delete);
    
    log.info("Observación general eliminada para Idoneidad ID: {}", idoniedadId);
    
    // Devolver el estado actualizado
    FormFieldStateDTO state = new FormFieldStateDTO();
    state.setIconClose(false);
    state.setCommentDisabled(true);
    state.setObservation("");
    return state;
}

    /**
 * Actualiza la observación general para un registro de idoneidad.
 */
public FormFieldStateDTO updateObservation(Long idoniedadId, String observation) {
    log.debug("Intentando actualizar observación para Idoneidad ID: {}", idoniedadId);
    Optional<RuiIdoniedad> idoniedadOptional = idoniedadRepository.findById(idoniedadId);
    
    if (idoniedadOptional.isEmpty()) {
        log.warn("Idoneidad no encontrada: {}", idoniedadId);
        FormFieldStateDTO state = new FormFieldStateDTO();
        state.setIconClose(false);
        state.setCommentDisabled(true);
        state.setObservation("Idoneidad no encontrada");
        return state;
    }

    RuiIdoniedad idoniedad = idoniedadOptional.get();
    
    // Verificar si el registro está activo
    if (idoniedad.getStatus() != 1 || idoniedad.getPersonId() == null || !idoniedad.getPersonId().isActive()) {
        log.warn("Idoneidad ID {} inactiva o persona asociada inactiva", idoniedadId);
        FormFieldStateDTO state = new FormFieldStateDTO();
        state.setIconClose(false);
        state.setCommentDisabled(true);
        state.setObservation("Idoneidad o persona inactiva");
        return state;
    }

    historyDetailsRepository.findByTableIdAndTableName(idoniedadId, "RUI_IDONIEDAD")
        .stream()
        .filter(d -> "general".equals(d.getFieldName()))
        .findFirst()
        .ifPresent(detail -> {
            detail.setObservation(observation);
            historyDetailsRepository.save(detail);
            log.info("Observación general actualizada para Idoneidad ID: {}", idoniedadId);
        });
    
    // Devolver el estado actualizado
    return getObservation(idoniedadId);
}

    /**
     * Encuentra o crea el historial activo para un intermediario asociado a la idoneidad.
     */
    private RuiIntermediaryHistory findOrCreateActiveHistory(RuiIntermediary intermediary) {
        Optional<RuiIntermediaryHistory> activeHistory = intermediaryHistoryRepository
            .findActiveByIntermediaryId(intermediary.getId());
        
        if (activeHistory.isPresent()) {
            return activeHistory.get();
        } else {
            RuiIntermediaryHistory history = new RuiIntermediaryHistory();
            history.setIntermediaryId(intermediary);
            history.setDatetime(new Date());
            history.setStatus((short) 1); // Activo
            
            return intermediaryHistoryRepository.save(history);
        }
    }

    /**
 * Verifica si un registro de idoneidad tiene observaciones
 */
    public boolean hasObservation(Long idoniedadId) {
        if (idoniedadId == null) {
            return false;
        }
        
        List<RuiHistoryDetails> details = historyDetailsRepository
            .findByTableIdAndTableName(idoniedadId, "RUI_IDONIEDAD");
        
        return details.stream()
            .anyMatch(d -> "general".equals(d.getFieldName()) && 
                    d.getObservation() != null && 
                    !d.getObservation().trim().isEmpty());
    }

    public List<RuiIdoniedad> findByIntermediaryIdForUpdate(Long intermediaryId) {
        return idoniedadRepository.findByIntermediaryId_Id(intermediaryId);
    }
    
    public RuiIdoniedad save(RuiIdoniedad idoneidad) {
        return idoniedadRepository.save(idoneidad);
    }

    /**
 * Guarda un registro de idoneidad en la base de datos
 */
public RuiIdoniedad saveIdoneidad(RuiIdoniedad idoneidad) {
    log.info("Guardando registro de idoneidad ID={}", idoneidad.getId());
    return idoniedadRepository.save(idoneidad);
}


}