package com.rui.basic.app.basic.service;

import com.rui.basic.app.basic.domain.entities.RuiInfraOperational;
import com.rui.basic.app.basic.repository.RuiInfraOperationalRepository;
import com.rui.basic.app.basic.web.dto.InfraestructuraOperativaDTO;

import org.springframework.stereotype.Service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class InfraestructuraOperativaService {

    private static final Logger log = LoggerFactory.getLogger(InfraestructuraOperativaService.class);
    private final RuiInfraOperationalRepository infraOperationalRepository;

    
    public InfraestructuraOperativaService(RuiInfraOperationalRepository infraOperationalRepository) {
        this.infraOperationalRepository = infraOperationalRepository;
    }

    public InfraestructuraOperativaDTO findByIntermediary(Long intermediaryId) {
        log.info("Buscando infraestructura operativa para intermediario: {}", intermediaryId);
        
        try {
            Optional<RuiInfraOperational> infraOperational = infraOperationalRepository.findByIntermediaryId(intermediaryId);
            
            if (infraOperational.isEmpty()) {
                log.info("No se encontró información de infraestructura operativa para el intermediario: {}", intermediaryId);
                return null;
            }
            
            return convertToDTO(infraOperational.get());
        } catch (Exception e) {
            log.error("Error al buscar infraestructura operativa: {}", e.getMessage(), e);
            // Intenta con consulta nativa si la JPQL falla
            try {
                Optional<RuiInfraOperational> infraOperational = infraOperationalRepository.findByIntermediaryIdNative(intermediaryId);
                
                if (infraOperational.isEmpty()) {
                    return null;
                }
                
                return convertToDTO(infraOperational.get());
            } catch (Exception ex) {
                log.error("Error al buscar infraestructura operativa con consulta nativa: {}", ex.getMessage(), ex);
                return null;
            }
        }
    }
    
    private InfraestructuraOperativaDTO convertToDTO(RuiInfraOperational infraOperational) {
        if (infraOperational == null) {
            return null;
        }
        
        InfraestructuraOperativaDTO dto = new InfraestructuraOperativaDTO();
        dto.setId(infraOperational.getId());
        dto.setPhone1(infraOperational.getPhone1());
        dto.setPhone2(infraOperational.getPhone2());
        dto.setPhone3(infraOperational.getPhone3());
        dto.setPhoneFax(infraOperational.getPhoneFax());
        dto.setEmail(infraOperational.getEmail());
        dto.setAddressServiceOffice(infraOperational.getAddressServiceOffice());
        dto.setSslService(infraOperational.getSslService());
        dto.setLicenseExpDate(infraOperational.getLicenseExpDate());
        
        // Para los checkboxes, por defecto marcados
        dto.setChecked(true);
        
        // Para los documentos de soporte, habría que implementar una lógica para determinar si están cargados
        // Por ahora, marcamos como true para permitir la descarga y visualización
        dto.setCamaraComercioUploaded(true);
        dto.setSoftwareCertificationUploaded(true);
        dto.setEquiposTecnologicosUploaded(true);
        
        return dto;
    }
}
