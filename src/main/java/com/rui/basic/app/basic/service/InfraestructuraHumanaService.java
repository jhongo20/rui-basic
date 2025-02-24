package com.rui.basic.app.basic.service;

import com.rui.basic.app.basic.domain.entities.RuiInfraHuman;
import com.rui.basic.app.basic.domain.entities.RuiPerson;
import com.rui.basic.app.basic.domain.entities.RuiSupport;
import com.rui.basic.app.basic.domain.entities.RuiWorkExperience;
import com.rui.basic.app.basic.repository.RuiInfraHumanRepository;
import com.rui.basic.app.basic.repository.RuiSupportRepository;
import com.rui.basic.app.basic.repository.RuiWorkExperienceRepository;
import com.rui.basic.app.basic.web.dto.ExperienciaLaboralDTO;
import com.rui.basic.app.basic.web.dto.InfrastructuraHumanaDTO;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class InfraestructuraHumanaService {

    private static final Logger log = LoggerFactory.getLogger(InfraestructuraHumanaService.class);
    private final RuiInfraHumanRepository infraHumanRepository;
    private final RuiWorkExperienceRepository workExperienceRepository;
    private final RuiSupportRepository ruiSupportRepository; 

   
    public InfraestructuraHumanaService(
            RuiInfraHumanRepository infraHumanRepository,
            RuiWorkExperienceRepository workExperienceRepository,
            IntermediaryService intermediaryService, 
            RuiSupportRepository ruiSupportRepository) {
        this.infraHumanRepository = infraHumanRepository;
        this.workExperienceRepository = workExperienceRepository;
        this.ruiSupportRepository = ruiSupportRepository;
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


    
}
