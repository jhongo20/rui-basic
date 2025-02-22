package com.rui.basic.app.basic.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.rui.basic.app.basic.domain.entities.RuiGenerics;
import com.rui.basic.app.basic.domain.entities.RuiIdoniedad;
import com.rui.basic.app.basic.domain.entities.RuiInfraHuman;
import com.rui.basic.app.basic.domain.entities.RuiInfraOperational;
import com.rui.basic.app.basic.domain.entities.RuiIntermediary;
import com.rui.basic.app.basic.domain.entities.RuiIntermediaryHistory;
import com.rui.basic.app.basic.domain.entities.RuiSupport;
import com.rui.basic.app.basic.domain.entities.RuiUser;
import com.rui.basic.app.basic.domain.enums.IntermediaryState;
import com.rui.basic.app.basic.exception.BusinessException;
import com.rui.basic.app.basic.repository.RuiGenericsRepository;
import com.rui.basic.app.basic.repository.RuiHistoryDetailsRepository;
import com.rui.basic.app.basic.repository.RuiIntermediaryHistoryRepository;
import com.rui.basic.app.basic.repository.RuiIntermediaryRepository;
import com.rui.basic.app.basic.repository.RuiSupportRepository;
import com.rui.basic.app.basic.service.email.EmailService;
import com.rui.basic.app.basic.web.dto.EmailTemplateDTO;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class IntermediaryService {

    private final RuiIntermediaryRepository intermediaryRepository;
    private final RuiHistoryDetailsRepository historyDetailsRepository;
    private final RuiIntermediaryHistoryRepository intermediaryHistoryRepository;
    private final EmailService emailService;
    private final AuditService auditService;
    private final RuiGenericsRepository genericsRepository;

    @Autowired
private RuiSupportRepository supportRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    public IntermediaryService(RuiIntermediaryRepository intermediaryRepository,
                             RuiHistoryDetailsRepository historyDetailsRepository,
                             RuiIntermediaryHistoryRepository intermediaryHistoryRepository,
                             EmailService emailService,
                             FileStorageService fileStorageService,
                             AuditService auditService,
                             RuiGenericsRepository genericsRepository) {
        this.intermediaryRepository = intermediaryRepository;
        this.historyDetailsRepository = historyDetailsRepository;
        this.intermediaryHistoryRepository = intermediaryHistoryRepository;
        this.emailService = emailService;
        this.auditService = auditService;
        this.genericsRepository = genericsRepository;
    }

    public void updateIntermediaryStatus(Long intermediaryId, IntermediaryState newState, String observations, RuiUser currentUser) throws BusinessException {
        try {
            RuiIntermediary intermediary = intermediaryRepository.findById(intermediaryId)
                .orElseThrow(() -> new EntityNotFoundException("Intermediary not found"));
    
            intermediary.setState(newState);
            intermediaryRepository.save(intermediary);
    
            // Crear registro histórico
            RuiIntermediaryHistory history = createHistory(intermediary, newState, observations, currentUser);
            
            // Enviar correo según el estado
            sendStatusEmail(intermediary, newState);
            
            // Registrar auditoría
            auditService.createTransaction("UPDATE", "CAMBIO DE ESTADO DE SOLICITUD", 
                "RUI_INTERMEDIARIES", intermediary.getId(), currentUser);
        } catch (BusinessException e) {
            log.error("Error en el proceso de actualización del intermediario: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado actualizando el intermediario: {}", e.getMessage());
            throw new BusinessException("Error actualizando el intermediario: " + e.getMessage(), e);
        }
    }

    private RuiIntermediaryHistory createHistory(RuiIntermediary intermediary, 
                                               IntermediaryState state,
                                               String observations, 
                                               RuiUser currentUser) {
        RuiIntermediaryHistory history = new RuiIntermediaryHistory();
        history.setIntermediaryId(intermediary);
        history.setFunctionaryId(currentUser);
        history.setObservation(observations);
        history.setDatetime(new Date());
        //history.setStatus(state.getState().shortValue());
        history.setIntermediaryState(state);
        
        return intermediaryHistoryRepository.save(history);
    }

    private void sendStatusEmail(RuiIntermediary intermediary, IntermediaryState state) {
        try {
            String name = getIntermediaryName(intermediary);
            String email = getIntermediaryEmail(intermediary);
            String address = getIntermediaryAddress(intermediary);
            String city = getIntermediaryCity(intermediary);
            String intermediaryType = intermediary.getTypeIntermediarieId().getValue();

            Map<String, String> templateVariables = new HashMap<>();
            templateVariables.put("name", name);
            templateVariables.put("address", address);
            templateVariables.put("city", city);
            templateVariables.put("radicateNumber", intermediary.getRadicateNumber());
            templateVariables.put("intermediaryType", intermediaryType);
            templateVariables.put("currentDate", getCurrentFormattedDate());

            EmailTemplateDTO emailDTO = new EmailTemplateDTO();
            emailDTO.setTo(email);
            emailDTO.setTemplateVariables(templateVariables);

            switch (state) {
                case DESISTED:
                    emailDTO.setTemplate("email/withdrawal.html");
                    emailDTO.setSubject("Desistimiento solicitud registro de intermediarios");
                    break;
                case RETIRED:
                    emailDTO.setTemplate("email/voluntary-withdrawal.html");
                    emailDTO.setSubject("Retiro de registro de intermediarios");
                    break;
                case APPROVED:
                    emailDTO.setTemplate("email/approval.html");
                    emailDTO.setSubject("Aprobación registro de intermediarios");
                    break;
                case TO_COMPLEMENT:
                    emailDTO.setTemplate("email/complement-required.html");
                    emailDTO.setSubject("Solicitud Complementar Información");
                    break;
                default:
                    log.warn("Estado no manejado: {}", state);
                    return;
            }

            try {
                emailService.sendEmail(emailDTO);
                log.info("Email sent successfully to {} for state {}", email, state);
            } catch (MessagingException e) {
                log.error("Error sending email to {} for state {}: {}", email, state, e.getMessage());
                // Aquí puedes decidir si quieres relanzar la excepción o manejarla de otra manera
                // Por ejemplo, podrías crear una excepción personalizada:
                throw new BusinessException("Error enviando correo electrónico: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Error preparing email for intermediary {}: {}", intermediary.getId(), e.getMessage());
            throw new BusinessException("Error preparando correo electrónico: " + e.getMessage(), e);
        }
    }

    private String getIntermediaryName(RuiIntermediary intermediary) {
        if (intermediary.getCompanyId() != null) {
            return intermediary.getCompanyId().getName();
        }
        return intermediary.getPersonId().getFirstName() + " " + 
               intermediary.getPersonId().getFirstSurname();
    }

    private String getIntermediaryEmail(RuiIntermediary intermediary) {
        return intermediary.getCompanyId() != null ? 
               intermediary.getCompanyId().getEmail() : 
               intermediary.getPersonId().getEmail();
    }

    private String getIntermediaryAddress(RuiIntermediary intermediary) {
        return intermediary.getCompanyId() != null ? 
               intermediary.getCompanyId().getAddress() : 
               intermediary.getPersonId().getAddress();
    }

    private String getIntermediaryCity(RuiIntermediary intermediary) {
        return intermediary.getCompanyId() != null ? 
               intermediary.getCompanyId().getCityId().getName() : 
               intermediary.getPersonId().getCityId().getName();
    }

    private String getCurrentFormattedDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(new Date());
    }

    public Long countPendingObservations(Long intermediaryId) {
        return historyDetailsRepository.countByIntermediaryHistoryId_IntermediaryId_Id(intermediaryId);
    }

    public void markAsReviewed(Long intermediaryId, RuiUser currentUser) {
        Long pendingObservations = countPendingObservations(intermediaryId);
        if (pendingObservations > 0) {
            throw new BusinessException("No se puede marcar como revisado porque existen observaciones pendientes");
        }
    
        updateIntermediaryStatus(intermediaryId, IntermediaryState.REVIEWED, null, currentUser);
    }

    public EmailTemplateDTO prepareStatusEmail(Long intermediaryId, IntermediaryState state) {
        try {
            RuiIntermediary intermediary = intermediaryRepository.findById(intermediaryId)
                .orElseThrow(() -> new EntityNotFoundException("Intermediary not found"));
    
            String name = getIntermediaryName(intermediary);
            String email = getIntermediaryEmail(intermediary);
            String address = getIntermediaryAddress(intermediary);
            String city = getIntermediaryCity(intermediary);
            String radicateNumber = intermediary.getRadicateNumber();
            String intermediaryType = intermediary.getTypeIntermediarieId().getValue();
    
            Map<String, String> templateVariables = new HashMap<>();
            templateVariables.put("name", name);
            templateVariables.put("address", address);
            templateVariables.put("city", city);
            templateVariables.put("radicateNumber", radicateNumber);
            templateVariables.put("intermediaryType", intermediaryType);
            templateVariables.put("currentDate", getCurrentFormattedDate());
    
            EmailTemplateDTO emailDTO = new EmailTemplateDTO();
            emailDTO.setTo(email);
            emailDTO.setTemplateVariables(templateVariables);
    
            switch (state) {
                case DESISTED:
                    emailDTO.setTemplate("email/withdrawal.html");
                    emailDTO.setSubject("Desistimiento solicitud registro de intermediarios");
                    break;
                case RETIRED:
                    emailDTO.setTemplate("email/voluntary-withdrawal.html");
                    emailDTO.setSubject("Retiro de registro de intermediarios");
                    break;
                case APPROVED:
                    emailDTO.setTemplate("email/approval.html");
                    emailDTO.setSubject("Aprobación registro de intermediarios");
                    break;
                case TO_COMPLEMENT:
                    emailDTO.setTemplate("email/complement-required.html");
                    emailDTO.setSubject("Solicitud Complementar Información");
                    break;
                default:
                    return null;
            }
    
            return emailDTO;
        } catch (Exception e) {
            log.error("Error preparing status email for intermediary {}: {}", intermediaryId, e.getMessage());
            throw new BusinessException("Error preparing status email: " + e.getMessage());
        }
    }

    public void saveSupport(Long intermediaryId, String filePath, String type) {
        try {
            RuiIntermediary intermediary = intermediaryRepository.findById(intermediaryId)
                .orElseThrow(() -> new EntityNotFoundException("Intermediary not found"));
    
            RuiSupport support = new RuiSupport();
            support.setFilename(getFileNameFromPath(filePath)); // Cambiado de setFileName a setFilename
        support.setRoute(getFilePathWithoutName(filePath)); // Cambiado de setFilePath a setRoute
        support.setExtencion(getFileExtension(filePath)); // Cambiado de setFileType a setExtencion
        support.setStatus(Short.valueOf("1"));
       // support.setIntermediaryId(intermediary);
    
            // Establecer las relaciones según el tipo de soporte 
            switch (type.toUpperCase()) {
                case "IDONIEDAD":
                    if (intermediary.getRuiIdoniedadList() != null && !intermediary.getRuiIdoniedadList().isEmpty()) {
                        RuiIdoniedad idoneidad = intermediary.getRuiIdoniedadList().get(0);
                        // Aquí necesitarías agregar la relación con idoneidad si es necesario
                    }
                    break;
                case "INFRA_HUMANA":
                    if (intermediary.getInfrastructureHumanId() != null) {
                        RuiInfraHuman infraHuman = intermediary.getInfrastructureHumanId();
                        // Aquí necesitarías agregar la relación con infraHuman si es necesario
                    }
                    break;
                case "INFRA_OPERACIONAL":
                    if (intermediary.getInfrastructureOperationalId() != null) {
                        RuiInfraOperational infraOperational = intermediary.getInfrastructureOperationalId();
                        // Determinar el subtipo de soporte operacional
                        if (filePath.contains("CC")) {
                            support.setInfraOperationalCc(infraOperational);
                            support.setExtencion("CC");
                        } else if (filePath.contains("HARD")) {
                            support.setInfraOperationalHard(infraOperational);
                            support.setExtencion("HARD");
                        } else if (filePath.contains("SOFT")) {
                            support.setInfraOperationalSoft(infraOperational);
                            support.setExtencion("SOFT");
                        } else if (filePath.contains("SSL")) {
                            support.setInfraOperationalSsl(infraOperational);
                            support.setExtencion("SSL");
                        }
                    }
                    break;
                case "WORK_EXPERIENCE":
                    // Manejar el caso de experiencia laboral si es necesario
                    break;
            }
    
            supportRepository.save(support);
            
            // Registrar la transacción en la auditoría
            auditService.createTransaction(
                "CREATE",
                "CREACIÓN DE SOPORTE",
                "RUI_SUPPORT",
                support.getId(),
                null
            );
    
        } catch (Exception e) {
            log.error("Error saving support for intermediary {}: {}", intermediaryId, e.getMessage());
            throw new BusinessException("Error saving support: " + e.getMessage());
        }
    }

    private String getFileNameFromPath(String filePath) {
        return filePath.substring(filePath.lastIndexOf('/') + 1);
    }

    private String getFilePathWithoutName(String filePath) {
        return filePath.substring(0, filePath.lastIndexOf('/'));
    }

    private String getFileExtension(String filePath) {
        return filePath.substring(filePath.lastIndexOf('.') + 1);
    }

    /**
     * Encuentra todos los intermediarios
     * @return Lista de intermediarios
     */
    public Page<RuiIntermediary> findAll(int page, int size, String sortField, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return intermediaryRepository.findAll(pageable);
    }
    
    public Page<RuiIntermediary> search(String term, int page, int size, String sort, String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            return intermediaryRepository.findBySearchTerm(term.trim(), pageable);
        } catch (Exception e) {
            log.error("Error searching intermediaries: {}", e.getMessage());
            throw new BusinessException("Error searching intermediaries: " + e.getMessage());
        }
    }

    /**
     * Encuentra un intermediario por su ID
     * @param id ID del intermediario
     * @return RuiIntermediary encontrado
     * @throws EntityNotFoundException si no se encuentra el intermediario
     */
    public RuiIntermediary findById(Long id) {
        return intermediaryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Intermediary not found with id: " + id));
    }
    
    /**
     * Encuentra un intermediario por su ID con todas sus relaciones cargadas
     * @param id ID del intermediario
     * @return RuiIntermediary encontrado con sus relaciones
     * @throws EntityNotFoundException si no se encuentra el intermediario
     */
    public RuiIntermediary findByIdWithDetails(Long id) {
        return intermediaryRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new EntityNotFoundException("Intermediary not found with id: " + id));
    }

    public String getDocumentTypeName(String documentTypeId) {
        try {
            Long id = Long.parseLong(documentTypeId);
            return genericsRepository.findById(id)
                .map(RuiGenerics::getValue)
                .orElse(documentTypeId);
        } catch (NumberFormatException e) {
            return documentTypeId;
        }
    }

    // En IntermediaryService
    public String getLastFunctionaryUsername(Long intermediaryId) {
        try {
            return intermediaryHistoryRepository.findFunctionaryUsername(intermediaryId);
        } catch (Exception e) {
            return "";
        }
    }
    
}
