package com.rui.basic.app.basic.service;

import com.rui.basic.app.basic.domain.entities.RuiInfraHuman;
import com.rui.basic.app.basic.domain.entities.RuiPerson;
import com.rui.basic.app.basic.domain.entities.RuiWorkExperience;
import com.rui.basic.app.basic.repository.RuiInfraHumanRepository;
import com.rui.basic.app.basic.repository.RuiWorkExperienceRepository;
import com.rui.basic.app.basic.web.dto.ExperienciaLaboralDTO;
import com.rui.basic.app.basic.web.dto.InfrastructuraHumanaDTO;

import org.springframework.beans.factory.annotation.Autowired;
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
    private final IntermediaryService intermediaryService; // Para usar getDocumentTypeName

    @Autowired
    public InfraestructuraHumanaService(
            RuiInfraHumanRepository infraHumanRepository,
            RuiWorkExperienceRepository workExperienceRepository,
            IntermediaryService intermediaryService) {
        this.infraHumanRepository = infraHumanRepository;
        this.workExperienceRepository = workExperienceRepository;
        this.intermediaryService = intermediaryService;
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
        List<RuiWorkExperience> workExperiences = workExperienceRepository.findByInfraHumanId_Id(infraHumanId);
        
        if (workExperiences.isEmpty()) {
            log.info("No se encontraron experiencias laborales para la infraestructura humana ID: {}", infraHumanId);
            return new ArrayList<>();
        }
        
        return workExperiences.stream()
                .map(this::convertToWorkExperienceDTO)
                .collect(Collectors.toList());
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
        
        ExperienciaLaboralDTO dto = new ExperienciaLaboralDTO();
        dto.setId(workExperience.getId());
        dto.setCompany(workExperience.getCompany());
        dto.setCharge(workExperience.getCharge());
        dto.setNameBoss(workExperience.getNameBoss());
        dto.setPhoneBoss(workExperience.getPhoneBoss());
        dto.setStartDate(workExperience.getStartDate());
        dto.setEndDate(workExperience.getEndDate());
        
        // Para determinar si hay soporte, se podría implementar alguna lógica aquí
        dto.setSupportUpload(true);
        dto.setChecked(true);
        
        return dto;
    }
}
