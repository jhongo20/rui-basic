package com.rui.basic.app.basic.service;

import com.rui.basic.app.basic.domain.entities.RuiHistoryDetails;
import com.rui.basic.app.basic.domain.entities.RuiInfraHuman;
import com.rui.basic.app.basic.domain.entities.RuiIntermediary;
import com.rui.basic.app.basic.domain.entities.RuiIntermediaryHistory;
import com.rui.basic.app.basic.domain.entities.RuiPerson;
import com.rui.basic.app.basic.domain.entities.RuiSupport;
import com.rui.basic.app.basic.domain.entities.RuiWorkExperience;
import com.rui.basic.app.basic.repository.RuiHistoryDetailsRepository;
import com.rui.basic.app.basic.repository.RuiInfraHumanRepository;
import com.rui.basic.app.basic.repository.RuiIntermediaryHistoryRepository;
import com.rui.basic.app.basic.repository.RuiIntermediaryRepository;
import com.rui.basic.app.basic.repository.RuiSupportRepository;
import com.rui.basic.app.basic.repository.RuiWorkExperienceRepository;
import com.rui.basic.app.basic.web.dto.ExperienciaLaboralDTO;
import com.rui.basic.app.basic.web.dto.FormFieldStateDTO;
import com.rui.basic.app.basic.web.dto.InfrastructuraHumanaDTO;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class InfraestructuraHumanaService {

    private static final Logger log = LoggerFactory.getLogger(InfraestructuraHumanaService.class);
    private final RuiInfraHumanRepository infraHumanRepository;
    private final RuiWorkExperienceRepository workExperienceRepository;
    private final RuiSupportRepository ruiSupportRepository; 
    private final RuiHistoryDetailsRepository historyDetailsRepository;
    private final RuiIntermediaryHistoryRepository intermediaryHistoryRepository;
    private final RuiIntermediaryRepository intermediaryRepository;

   
    public InfraestructuraHumanaService(
            RuiInfraHumanRepository infraHumanRepository,
            RuiWorkExperienceRepository workExperienceRepository,
            IntermediaryService intermediaryService, 
            RuiSupportRepository ruiSupportRepository,
            RuiHistoryDetailsRepository historyDetailsRepository,
            RuiIntermediaryHistoryRepository intermediaryHistoryRepository,
            RuiIntermediaryRepository intermediaryRepository) {
        this.infraHumanRepository = infraHumanRepository;
        this.workExperienceRepository = workExperienceRepository;
        this.ruiSupportRepository = ruiSupportRepository;
        this.historyDetailsRepository = historyDetailsRepository;
        this.intermediaryHistoryRepository = intermediaryHistoryRepository;
        this.intermediaryRepository = intermediaryRepository;
    }

    public InfrastructuraHumanaDTO findByIntermediary(Long intermediaryId) {
        log.info("Buscando infraestructura humana para intermediario: {}", intermediaryId);
        
        try {
            Optional<RuiInfraHuman> infraHuman = infraHumanRepository.findByIntermediaryId(intermediaryId);
            
            if (infraHuman.isEmpty()) {
                log.info("No se encontró información de infraestructura humana para el intermediario: {}", intermediaryId);
                return null;
            }
            
            return convertToDTO(infraHuman.get());
        } catch (Exception e) {
            log.error("Error al buscar infraestructura humana: {}", e.getMessage(), e);
            return null;
        }
    }
    
    public List<ExperienciaLaboralDTO> findWorkExperienceByInfraHuman(Long infraHumanId) {
        log.info("Buscando experiencias laborales activas para infraestructura humana ID: {}", infraHumanId);
        
        try {
            List<RuiWorkExperience> workExperiences = workExperienceRepository.findByInfraHumanId_Id(infraHumanId);
            
            log.debug("Encontradas {} experiencias laborales activas", workExperiences.size());
            
            if (workExperiences.isEmpty()) {
                log.info("No se encontraron experiencias laborales activas para la infraestructura humana ID: {}", 
                    infraHumanId);
                return new ArrayList<>();
            }
            
            // Log detallado de cada experiencia laboral
            workExperiences.forEach(exp -> {
                log.debug("Experiencia laboral - ID: {}, Empresa: {}, Cargo: {}, Estado: {}", 
                    exp.getId(), 
                    exp.getCompany(), 
                    exp.getCharge(),
                    exp.getStatus());
            });
            
            return workExperiences.stream()
                    .map(this::convertToWorkExperienceDTO)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error al buscar experiencias laborales para infraestructura humana ID: {} - Error: {}", 
                infraHumanId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    private InfrastructuraHumanaDTO convertToDTO(RuiInfraHuman infraHuman) {
        if (infraHuman == null) {
            return null;
        }
        
        InfrastructuraHumanaDTO dto = new InfrastructuraHumanaDTO();
        dto.setId(infraHuman.getId());
        
        // Información del abogado
        if (infraHuman.getLawyerId() != null) {
            RuiPerson lawyer = infraHuman.getLawyerId();
            
            // Convierte el Long documentType a String para el DTO
            if (lawyer.getDocumentType() != null) {
                dto.setDocumentType(lawyer.getDocumentType().toString());
            }
            
            dto.setDocumentNumber(lawyer.getDocumentNumber());
            dto.setFirstName(lawyer.getFirstName());
            dto.setSecondName(lawyer.getSecondName());
            dto.setFirstSurname(lawyer.getFirstSurname());
            dto.setSecondSurname(lawyer.getSecondSurname());
        }
        
        dto.setChecked(true);
        
        return dto;
    }
    
    private ExperienciaLaboralDTO convertToWorkExperienceDTO(RuiWorkExperience workExperience) {
        if (workExperience == null) {
            return null;
        }
        
        log.debug("Convirtiendo experiencia laboral a DTO - ID: {}", workExperience.getId());
        
        ExperienciaLaboralDTO dto = new ExperienciaLaboralDTO();
        dto.setId(workExperience.getId());
        dto.setCompany(workExperience.getCompany());
        dto.setCharge(workExperience.getCharge());
        dto.setNameBoss(workExperience.getNameBoss());
        dto.setPhoneBoss(workExperience.getPhoneBoss());
        dto.setStartDate(workExperience.getStartDate());
        dto.setEndDate(workExperience.getEndDate());
        dto.setStatus(workExperience.getStatus());
        
        // Verificar soporte (único)
        Optional<RuiSupport> supportOpt = ruiSupportRepository.findFirstByWorkExperienceId(workExperience);
        
        dto.setSupportUpload(supportOpt.isPresent());
        
        if (supportOpt.isPresent()) {
            RuiSupport support = supportOpt.get();
            log.debug("Soporte encontrado - ID: {}, Filename: {}", 
                support.getId(), 
                support.getFilename());
        } else {
            log.debug("No se encontró soporte para la experiencia laboral ID: {}", 
                workExperience.getId());
        }
        
        dto.setChecked(true);
        return dto;
    }

    public List<ExperienciaLaboralDTO> findWorkExperienceByIntermediary(Long intermediaryId) {
        log.info("Buscando experiencias laborales para intermediario ID: {}", intermediaryId);
        
        try {
            // Usar el método del repositorio que modificamos para usar 'intermediary' en lugar de 'intermediaryId'
            List<RuiWorkExperience> experiences = workExperienceRepository
                .findByInfraHumanId_Intermediary_Id(intermediaryId);
            
            if (experiences.isEmpty()) {
                log.info("No se encontraron experiencias laborales para el intermediario ID: {}", intermediaryId);
                return new ArrayList<>();
            }
            
            return experiences.stream()
                    .map(this::convertToWorkExperienceDTO)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error al buscar experiencias laborales para intermediario ID: {} - Error: {}", 
                intermediaryId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    //endpoints para las observaciones de la infraestructura humana
   /**
     * Obtiene el estado de la observación para una experiencia laboral
     */
    public FormFieldStateDTO getWorkExpObservation(Long workExpId) {
        log.debug("Buscando observación para experiencia laboral ID: {}", workExpId);
        
        Optional<RuiWorkExperience> workExpOptional = workExperienceRepository.findById(workExpId);
        if (workExpOptional.isEmpty()) {
            throw new EntityNotFoundException("Experiencia laboral no encontrada con ID: " + workExpId);
        }
        
        RuiWorkExperience workExp = workExpOptional.get();
        if (!workExp.isActive()) {
            log.warn("Experiencia laboral inactiva con ID: {}", workExpId);
            FormFieldStateDTO state = new FormFieldStateDTO();
            state.setIconClose(false);
            state.setCommentDisabled(true);
            state.setObservation("Esta experiencia laboral está inactiva");
            return state;
        }
        
        // Buscar observación en RuiHistoryDetails
        List<RuiHistoryDetails> details = historyDetailsRepository.findByTableIdAndTableName(workExpId, "RUI_WORK_EXPERIENCE");
        Optional<RuiHistoryDetails> detail = details.stream()
                .filter(d -> "general".equals(d.getFieldName()))
                .findFirst();
        
        FormFieldStateDTO state = new FormFieldStateDTO();
        if (detail.isPresent()) {
            state.setIconClose(true);
            state.setCommentDisabled(false);
            state.setObservation(detail.get().getObservation());
        } else {
            state.setIconClose(false);
            state.setCommentDisabled(true);
            state.setObservation("");
        }
        
        return state;
    }

    /**
     * Crea una observación general para una experiencia laboral.
     */
    public FormFieldStateDTO createWorkExpObservation(Long workExpId) {
        log.debug("Creando observación para experiencia laboral ID: {}", workExpId);
        
        Optional<RuiWorkExperience> workExpOptional = workExperienceRepository.findById(workExpId);
        if (workExpOptional.isEmpty()) {
            throw new EntityNotFoundException("Experiencia laboral no encontrada con ID: " + workExpId);
        }
        
        RuiWorkExperience workExp = workExpOptional.get();
        if (!workExp.isActive()) {
            log.warn("Experiencia laboral inactiva con ID: {}", workExpId);
            FormFieldStateDTO state = new FormFieldStateDTO();
            state.setIconClose(false);
            state.setCommentDisabled(true);
            state.setObservation("Esta experiencia laboral está inactiva");
            return state;
        }
        
        // Obtener el intermediario asociado
        RuiInfraHuman infraHuman = workExp.getInfraHumanId();
        if (infraHuman == null) {
            throw new EntityNotFoundException("Infraestructura humana no encontrada para experiencia laboral ID: " + workExpId);
        }
        
        Optional<RuiIntermediary> intermediaryOpt = intermediaryRepository.findByInfrastructureHumanId(infraHuman.getId());
        if (intermediaryOpt.isEmpty()) {
            throw new EntityNotFoundException("Intermediario no encontrado para infraestructura humana ID: " + infraHuman.getId());
        }
        
        RuiIntermediary intermediary = intermediaryOpt.get();
        
        // Buscar o crear el historial activo
        RuiIntermediaryHistory history = findOrCreateActiveHistory(intermediary);
        
        // Verificar si ya existe una observación
        List<RuiHistoryDetails> details = historyDetailsRepository.findByTableIdAndTableName(workExpId, "RUI_WORK_EXPERIENCE");
        Optional<RuiHistoryDetails> existingDetail = details.stream()
                .filter(d -> "general".equals(d.getFieldName()))
                .findFirst();
        
        if (existingDetail.isPresent()) {
            log.info("Ya existe una observación para la experiencia laboral ID: {}", workExpId);
            return getWorkExpObservation(workExpId);
        }
        
        // Crear nueva observación
        RuiHistoryDetails newDetail = new RuiHistoryDetails();
        newDetail.setIntermediaryHistoryId(history);
        newDetail.setTableName("RUI_WORK_EXPERIENCE");
        newDetail.setTableId(workExpId);
        newDetail.setFieldName("general");
        newDetail.setObservation("");
        
        historyDetailsRepository.save(newDetail);
        log.info("Creada nueva observación para experiencia laboral ID: {}", workExpId);
        
        return getWorkExpObservation(workExpId);
    }

    /**
     * Elimina la observación general para una experiencia laboral.
     */
    public FormFieldStateDTO removeWorkExpObservation(Long workExpId) {
        log.debug("Eliminando observación para experiencia laboral ID: {}", workExpId);
        
        Optional<RuiWorkExperience> workExpOptional = workExperienceRepository.findById(workExpId);
        if (workExpOptional.isEmpty()) {
            throw new EntityNotFoundException("Experiencia laboral no encontrada con ID: " + workExpId);
        }
        
        // Buscar y eliminar la observación
        List<RuiHistoryDetails> details = historyDetailsRepository.findByTableIdAndTableName(workExpId, "RUI_WORK_EXPERIENCE");
        details.stream()
                .filter(d -> "general".equals(d.getFieldName()))
                .findFirst()
                .ifPresent(historyDetailsRepository::delete);
        
        log.info("Eliminada observación para experiencia laboral ID: {}", workExpId);
        
        FormFieldStateDTO state = new FormFieldStateDTO();
        state.setIconClose(false);
        state.setCommentDisabled(true);
        state.setObservation("");
        
        return state;
    }

    /**
     * Actualiza la observación general para una experiencia laboral.
     */
    public FormFieldStateDTO updateWorkExpObservation(Long workExpId, String observation) {
        log.debug("Actualizando observación para experiencia laboral ID: {}", workExpId);
        
        Optional<RuiWorkExperience> workExpOptional = workExperienceRepository.findById(workExpId);
        if (workExpOptional.isEmpty()) {
            throw new EntityNotFoundException("Experiencia laboral no encontrada con ID: " + workExpId);
        }
        
        // Buscar y actualizar la observación
        List<RuiHistoryDetails> details = historyDetailsRepository.findByTableIdAndTableName(workExpId, "RUI_WORK_EXPERIENCE");
        Optional<RuiHistoryDetails> detailOpt = details.stream()
                .filter(d -> "general".equals(d.getFieldName()))
                .findFirst();
        
        if (detailOpt.isPresent()) {
            RuiHistoryDetails detail = detailOpt.get();
            detail.setObservation(observation);
            historyDetailsRepository.save(detail);
            log.info("Actualizada observación para experiencia laboral ID: {}", workExpId);
        } else {
            log.warn("No se encontró observación para actualizar en experiencia laboral ID: {}", workExpId);
        }
        
        return getWorkExpObservation(workExpId);
    }

    /**
     * Encuentra o crea un historial activo para un intermediario
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
     * Método auxiliar para encontrar el intermediario asociado a una experiencia laboral
     */
    private Optional<RuiIntermediary> findIntermediaryByWorkExpId(Long workExpId) {
        Optional<RuiWorkExperience> workExpOptional = workExperienceRepository.findById(workExpId);
        if (workExpOptional.isEmpty()) {
            return Optional.empty();
        }
        
        RuiWorkExperience workExp = workExpOptional.get();
        if (workExp.getInfraHumanId() == null) {
            return Optional.empty();
        }
        
        Long infraHumanId = workExp.getInfraHumanId().getId();
        
        return intermediaryRepository.findByInfrastructureHumanId(infraHumanId);
    }

    private static final Set<String> GENERAL_FIELDS = Set.of(
        "nit", "business_name", "department_id", "city_id", "address", "email", "phone",
        "document_type", "document_number", "first_name", "second_name", "first_surname", "second_surname", "cellphone",
        "infra_document_type", "infra_document_number", "infra_first_name", "infra_second_name", 
        "infra_first_surname", "infra_second_surname"
    );

    public Map<String, FormFieldStateDTO> getFieldStates(Long id) {
        Map<String, FormFieldStateDTO> fieldStates = new HashMap<>();
        
        // Buscar todas las observaciones asociadas a este intermediario
        List<RuiHistoryDetails> observations = historyDetailsRepository.findByIntermediaryId(id);
        
        // Procesar observaciones
        for (RuiHistoryDetails observation : observations) {
            String fieldName = observation.getFieldName();
            
            // Solo procesar campos de Información General y ahora también de Infraestructura Humana
            if (GENERAL_FIELDS.contains(fieldName) || fieldName.startsWith("infra_")) {
                FormFieldStateDTO state = new FormFieldStateDTO();
                state.setIconClose(true);
                state.setCommentDisabled(false);
                state.setObservation(observation.getObservation());
                
                fieldStates.put(fieldName, state);
            }
        }
        
        return fieldStates;
    }
    
}
