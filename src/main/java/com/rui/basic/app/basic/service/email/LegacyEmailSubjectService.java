package com.rui.basic.app.basic.service.email;

import com.rui.basic.app.basic.domain.entities.RuiGenerics;
import com.rui.basic.app.basic.repository.RuiGenericsRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LegacyEmailSubjectService {
    
    private static final Logger log = LoggerFactory.getLogger(LegacyEmailSubjectService.class);
    
    private final RuiGenericsRepository ruiGenericsRepository;
    
    public LegacyEmailSubjectService(RuiGenericsRepository ruiGenericsRepository) {
        this.ruiGenericsRepository = ruiGenericsRepository;
    }
    
    public String getSubjectById(int tipoAsunto) {
        try {
            Long idAsunto;
            switch (tipoAsunto) {
                case 1: idAsunto = 27L; break;
                case 2: idAsunto = 29L; break;
                case 3: idAsunto = 31L; break;
                case 4: idAsunto = 41L; break;
                case 5: idAsunto = 45L; break;
                case 6: idAsunto = 47L; break;
                case 7: idAsunto = 49L; break;
                case 8: idAsunto = 51L; break;
                case 9: idAsunto = 53L; break;
                default: return "Notificación Sistema RUI";
            }
            
            return ruiGenericsRepository.findByIdAndStatus(idAsunto, 1)
                .map(RuiGenerics::getValue)
                .orElse("Notificación Sistema RUI");
            
        } catch (Exception e) {
            log.error("Error al obtener asunto legado: {}", e.getMessage(), e);
            return "Notificación Sistema RUI";
        }
    }
}