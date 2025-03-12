package com.rui.basic.app.basic.service;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.rui.basic.app.basic.domain.entities.RuiAssignment;
import com.rui.basic.app.basic.domain.entities.RuiCompany;
import com.rui.basic.app.basic.domain.entities.RuiGenerics;
import com.rui.basic.app.basic.domain.entities.RuiHistoryDetails;
import com.rui.basic.app.basic.domain.entities.RuiIdoniedad;
import com.rui.basic.app.basic.domain.entities.RuiInfraHuman;
import com.rui.basic.app.basic.domain.entities.RuiInfraOperational;
import com.rui.basic.app.basic.domain.entities.RuiIntermediary;
import com.rui.basic.app.basic.domain.entities.RuiIntermediaryHistory;
import com.rui.basic.app.basic.domain.entities.RuiSupport;
import com.rui.basic.app.basic.domain.entities.RuiUser;
import com.rui.basic.app.basic.domain.enums.IntermediaryState;
import com.rui.basic.app.basic.exception.BusinessException;
import com.rui.basic.app.basic.repository.CompanyRepository;
import com.rui.basic.app.basic.repository.RuiAssignmentRepository;
import com.rui.basic.app.basic.repository.RuiGenericsRepository;
import com.rui.basic.app.basic.repository.RuiHistoryDetailsRepository;
import com.rui.basic.app.basic.repository.RuiIntermediaryHistoryRepository;
import com.rui.basic.app.basic.repository.RuiIntermediaryRepository;
import com.rui.basic.app.basic.repository.RuiPersonRepository;
import com.rui.basic.app.basic.repository.RuiSupportRepository;
import com.rui.basic.app.basic.repository.RuiUserRepository;
import com.rui.basic.app.basic.service.email.EmailService;
import com.rui.basic.app.basic.web.dto.EmailTemplateDTO;
import com.rui.basic.app.basic.web.dto.FormFieldStateDTO;

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
    private final RuiUserRepository userRepository;
    private final RuiPersonRepository ruiPersonRepository;
    private final RuiAssignmentRepository assignmentRepository;
    private final CompanyRepository companyRepository;
    

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
            RuiGenericsRepository genericsRepository,
            RuiUserRepository userRepository,
            RuiPersonRepository ruiPersonRepository,
            RuiAssignmentRepository assignmentRepository,
            CompanyRepository companyRepository) {
        this.intermediaryRepository = intermediaryRepository;
        this.historyDetailsRepository = historyDetailsRepository;
        this.intermediaryHistoryRepository = intermediaryHistoryRepository;
        this.emailService = emailService;
        this.auditService = auditService;
        this.genericsRepository = genericsRepository;
        this.userRepository = userRepository;
        this.ruiPersonRepository = ruiPersonRepository;
        this.assignmentRepository = assignmentRepository;
        this.companyRepository = companyRepository;
    }

    public void updateIntermediaryStatus(Long intermediaryId, IntermediaryState newState, String observations,
            RuiUser currentUser) throws BusinessException {
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
        // history.setStatus(state.getState().shortValue());
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
                // Aquí puedes decidir si quieres relanzar la excepción o manejarla de otra
                // manera
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
        return intermediary.getCompanyId() != null ? intermediary.getCompanyId().getEmail()
                : intermediary.getPersonId().getEmail();
    }

    private String getIntermediaryAddress(RuiIntermediary intermediary) {
        return intermediary.getCompanyId() != null ? intermediary.getCompanyId().getAddress()
                : intermediary.getPersonId().getAddress();
    }

    private String getIntermediaryCity(RuiIntermediary intermediary) {
        return intermediary.getCompanyId() != null ? intermediary.getCompanyId().getCityId().getName()
                : intermediary.getPersonId().getCityId().getName();
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
                    null);

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
     * 
     * @return Lista de intermediarios
     *         public Page<RuiIntermediary> findAll(int page, int size, String
     *         sortField, String sortDirection) {
     *         Sort sort = Sort.by(Sort.Direction.fromString(sortDirection),
     *         sortField);
     *         Pageable pageable = PageRequest.of(page, size, sort);
     * 
     *         // Agregar log de la consulta SQL directa
     *         log.debug("Ejecutando consulta directa a RUI_INTERMEDIARIES");
     *         List<Object[]> rawStates =
     *         intermediaryRepository.findAllStatesDirectly();
     *         for (Object[] row : rawStates) {
     *         log.debug("ID: {}, Estado directo de BD: {}", row[0], row[1]);
     *         }
     * 
     *         Page<RuiIntermediary> results =
     *         intermediaryRepository.findAll(pageable);
     * 
     *         results.getContent().forEach(intermediary -> {
     *         log.debug("Intermediary from JPA - ID: {}, Raw State Value: {},
     *         Mapped State: {}, State Name: {}",
     *         intermediary.getId(),
     *         // Valor raw del estado
     *         intermediary.getState() != null ? intermediary.getState().getState()
     *         : "null",
     *         // Estado mapeado
     *         intermediary.getState(),
     *         // Nombre del estado
     *         intermediary.getState() != null ? intermediary.getState().getName() :
     *         "null"
     *         );
     *         });
     * 
     *         return results;
     *         }
     */
    public Page<RuiIntermediary> findAll(int page, int size, String sortField, String sortDirection) {
        try {
            Sort sort;
            if (sortField != null && !sortField.isEmpty()) {
                // Si se proporciona un campo de ordenamiento, lo usamos
                sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
            } else {
                // Ordenamiento por defecto: último registro primero
                sort = Sort.by(
                        Sort.Order.desc("lastUpdate"), // Primero por fecha de actualización
                        Sort.Order.desc("radicateNumber") // Luego por número de radicado
                );
            }

            Pageable pageable = PageRequest.of(page, size, sort);
            return intermediaryRepository.findAllWithDetails(pageable);

        } catch (Exception e) {
            log.error("Error obteniendo lista de intermediarios: {}", e.getMessage());
            throw new BusinessException("Error obteniendo lista de intermediarios", e);
        }
    }

    // Método de búsqueda mejorado
    public Page<RuiIntermediary> search(String term, int page, int size, String sort, String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

            if (term == null || term.trim().isEmpty()) {
                return findAll(page, size, sort, direction);
            }

            return intermediaryRepository.findBySearchTerm(term.trim(), pageable);
        } catch (Exception e) {
            log.error("Error searching intermediaries: {}", e.getMessage());
            throw new BusinessException("Error searching intermediaries: " + e.getMessage());
        }
    }

    /**
     * Encuentra un intermediario por su ID
     * 
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
     * 
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
            if (intermediaryId == null) {
                return "No asignado";
            }

            String username = intermediaryHistoryRepository.findFunctionaryUsername(intermediaryId);
            return username != null ? username : "No asignado";

        } catch (Exception e) {
            log.error("Error obteniendo username del funcionario para intermediario {}: {}", intermediaryId,
                    e.getMessage());
            return "No asignado";
        }
    }

    public String getStateColorClass(IntermediaryState state) {
        if (state == null)
            return "bg-secondary";

        return switch (state) {
            case OPENING -> "bg-primary";
            case DILIGENTED -> "bg-info";
            case TO_COMPLEMENT -> "bg-warning";
            case COMPLEMENTED -> "bg-info";
            case APPROVED -> "bg-success";
            case REVIEWED -> "bg-primary";
            case UPDATED -> "bg-info";
            case RETIRED, DESISTED, PENALIZED -> "bg-danger";
            case RENEWAL -> "bg-info";
            default -> "bg-secondary";
        };
    }

    // Metodos para las observaciones
    public FormFieldStateDTO getFieldState(Long intermediaryId, String field) {
        List<RuiHistoryDetails> details = historyDetailsRepository
                .findByIntermediaryId(intermediaryId);

        FormFieldStateDTO state = new FormFieldStateDTO();

        Optional<RuiHistoryDetails> detail = details.stream()
                .filter(d -> d.getFieldName().equals(field))
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

        return state;
    }

    public void createObservation(Long intermediaryId, String field) {
        // Lógica para crear una observación
        RuiIntermediary intermediary = findById(intermediaryId);

        // Buscar historial activo o crear uno nuevo
        RuiIntermediaryHistory history = findOrCreateActiveHistory(intermediary);

        // Determinar tableName y tableId
        String tableName = determineTableName(field);
        Long tableId = determineTableId(intermediary, field);

        // Crear detalle de historial
        RuiHistoryDetails detail = new RuiHistoryDetails();
        detail.setIntermediaryHistoryId(history);
        detail.setFieldName(field);
        detail.setObservation(""); // Inicialmente vacío
        detail.setTableName(tableName);
        detail.setTableId(tableId);

        historyDetailsRepository.save(detail);
    }

    /**
     * Método auxiliar para determinar el nombre de la tabla basado en el campo
     * 
     * @param field El nombre del campo
     * @return El nombre de la tabla correspondiente
     */
    private String determineTableName(String field) {
        if (field == null) {
            return "UNKNOWN";
        }

        // Mapeo de campos a tablas
        Map<String, String> tableMap = new HashMap<>();

        // Campos de la compañía
        tableMap.put("nit", "RUI_COMPANY");
        tableMap.put("business_name", "RUI_COMPANY");
        tableMap.put("department_id", "RUI_COMPANY");
        tableMap.put("city_id", "RUI_COMPANY");
        tableMap.put("address", "RUI_COMPANY");
        tableMap.put("email", "RUI_COMPANY");
        tableMap.put("phone", "RUI_COMPANY");

        // Campos de persona
        tableMap.put("document_type", "RUI_PERSON");
        tableMap.put("document_number", "RUI_PERSON");
        tableMap.put("first_name", "RUI_PERSON");
        tableMap.put("second_name", "RUI_PERSON");
        tableMap.put("first_surname", "RUI_PERSON");
        tableMap.put("second_surname", "RUI_PERSON");
        tableMap.put("cellphone", "RUI_PERSON");

        // Campos de infraestructura humana
        tableMap.put("infra_document_type", "RUI_PERSONS"); // Asumiendo que son los datos del profesional
        tableMap.put("infra_document_number", "RUI_PERSONS");
        tableMap.put("infra_first_name", "RUI_PERSONS");
        tableMap.put("infra_second_name", "RUI_PERSONS");
        tableMap.put("infra_first_surname", "RUI_PERSONS");
        tableMap.put("infra_second_surname", "RUI_PERSONS");

        // Campos de experiencia laboral
        if (field.equals("work_exp")) {
            return "RUI_WORK_EXPERIENCE";
        }

        // Campos de infraestructura operativa
        tableMap.put("operativa_camara", "RUI_INFRA_OPERATIONAL");
        tableMap.put("operativa_software", "RUI_INFRA_OPERATIONAL");
        tableMap.put("operativa_equipos", "RUI_INFRA_OPERATIONAL");
        tableMap.put("operativa_phone1", "RUI_INFRA_OPERATIONAL");
        tableMap.put("operativa_phone2", "RUI_INFRA_OPERATIONAL");
        tableMap.put("operativa_phone3", "RUI_INFRA_OPERATIONAL");
        tableMap.put("operativa_phone_fax", "RUI_INFRA_OPERATIONAL");
        tableMap.put("operativa_email", "RUI_INFRA_OPERATIONAL");
        tableMap.put("operativa_address", "RUI_INFRA_OPERATIONAL");

        // Campo de firma digitalizada
        tableMap.put("firma_digitalizada", "RUI_SUPPORT"); // Asumiendo que la firma se guarda en la tabla de soporte

        // Usar el mapeo o determinar el nombre de la tabla por prefijo
        if (tableMap.containsKey(field)) {
            return tableMap.get(field);
        }

        // Si no está en el mapeo, intentar determinar por prefijo
        if (field.startsWith("infra_")) {
            return "RUI_INFRA_HUMAN";
        } else if (field.startsWith("operativa_")) {
            return "RUI_INFRA_OPERATIONAL";
        }

        // Registrar campos desconocidos para facilitar la depuración
        log.warn("Campo no mapeado a ninguna tabla: {}", field);
        return "UNKNOWN";
    }

    // Método auxiliar para determinar el ID de la tabla
    private Long determineTableId(RuiIntermediary intermediary, String field) {
        if (intermediary.getCompanyId() != null) {
            // Campos de compañía
            if ("nit".equals(field) || "business_name".equals(field) ||
                    "department_id".equals(field) || "city_id".equals(field) ||
                    "address".equals(field) || "email".equals(field) ||
                    "phone".equals(field)) {
                return intermediary.getCompanyId().getId();
            }
        }
        if (intermediary.getPersonId() != null) {
            // Campos de persona
            if ("document_type".equals(field) || "document_number".equals(field) ||
                    "first_name".equals(field) || "second_name".equals(field) ||
                    "first_surname".equals(field) || "second_surname".equals(field) ||
                    "cellphone".equals(field)) {
                return intermediary.getPersonId().getId();
            }
            // Campos compartidos (pueden venir de persona o compañía, usamos persona si
            // existe)
            if ("department_id".equals(field) || "city_id".equals(field) ||
                    "address".equals(field) || "email".equals(field) ||
                    "phone".equals(field)) {
                return intermediary.getPersonId().getId();
            }
        }
        return null;
    }

    public void removeObservation(Long intermediaryId, String field) {
        // Lógica para eliminar una observación
        List<RuiHistoryDetails> details = historyDetailsRepository
                .findByIntermediaryId(intermediaryId);

        details.stream()
                .filter(d -> d.getFieldName().equals(field))
                .findFirst()
                .ifPresent(detail -> historyDetailsRepository.delete(detail));
    }

    public void updateObservation(Long intermediaryId, String field, String observation) {
        // Lógica para actualizar una observación
        List<RuiHistoryDetails> details = historyDetailsRepository
                .findByIntermediaryId(intermediaryId);

        details.stream()
                .filter(d -> d.getFieldName().equals(field))
                .findFirst()
                .ifPresent(detail -> {
                    detail.setObservation(observation);
                    historyDetailsRepository.save(detail);
                });
    }

    private RuiIntermediaryHistory findOrCreateActiveHistory(RuiIntermediary intermediary) {
        // Buscar historial activo
        Optional<RuiIntermediaryHistory> activeHistory = intermediaryHistoryRepository
                .findActiveByIntermediaryId(intermediary.getId());

        if (activeHistory.isPresent()) {
            return activeHistory.get();
        } else {
            // Crear nuevo historial
            RuiIntermediaryHistory history = new RuiIntermediaryHistory();
            history.setIntermediaryId(intermediary);
            history.setDatetime(new Date()); // Usar datetime en lugar de creationDate
            history.setStatus((short) 1); // Activo

            return intermediaryHistoryRepository.save(history);
        }
    }

    public Map<String, FormFieldStateDTO> getFieldStates(Long intermediaryId) {
        Map<String, FormFieldStateDTO> states = new HashMap<>();
        List<RuiHistoryDetails> details = historyDetailsRepository.findByIntermediaryId(intermediaryId);

        // Campos de empresa y persona
        String[] fields = {
                "nit", "business_name", "department_id", "city_id", "address", "email", "phone",
                "document_type", "document_number", "first_name", "second_name", "first_surname",
                "second_surname", "cellphone"
        };

        for (String field : fields) {
            FormFieldStateDTO state = new FormFieldStateDTO();
            Optional<RuiHistoryDetails> detail = details.stream()
                    .filter(d -> d.getFieldName().equals(field))
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
            states.put(field, state);
        }
        return states;
    }

    // --------------------------------------------------------------------------------
    // Métodos para la paginación y búsqueda de usuarios para usuarios registrados
    public Page<RuiIntermediary> findByUser(String username, int page, int size, String sortField,
            String sortDirection) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
            Pageable pageable = PageRequest.of(page, size, sort);

            // Obtener el usuario por username
            RuiUser user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));

            // Verificar si el usuario tiene una persona asociada
            if (user.getPerson() != null) {
                return intermediaryRepository.findAllByPersonId(user.getPerson(), pageable);
            } else {
                RuiCompany company = findCompanyByUser(user);
                if (company != null) {
                    return intermediaryRepository.findAllByCompanyId(company, pageable);
                } else {
                    return new PageImpl<>(Collections.emptyList(), pageable, 0);
                }
            }
        } catch (Exception e) {
            log.error("Error al buscar registros para el usuario {}: {}", username, e.getMessage(), e);
            throw new BusinessException("Error al buscar registros del usuario", e);
        }
    }

    // Método auxiliar para encontrar la empresa asociada al usuario
    private RuiCompany findCompanyByUser(RuiUser user) {
        return user.getCompany();
    }

    public void saveSupportForIdoneidad(Long idoneidadId, String filePath, String type) {
        try {
            RuiIdoniedad idoneidad = intermediaryRepository.findIdoneidadById(idoneidadId)
                    .orElseThrow(() -> new EntityNotFoundException("Idoneidad not found with id: " + idoneidadId));

            RuiSupport support = supportRepository.findByIdoniedadIdAndPersonStatus(idoneidad, 1) // Suponiendo status
                                                                                                  // activo = 1
                    .orElse(new RuiSupport());

            support.setIdoniedadId(idoneidad);
            support.setFilename(getFileNameFromPath(filePath));
            support.setRoute(getFilePathWithoutName(filePath));
            support.setExtencion(getFileExtension(filePath));
            support.setStatus((short) 1);

            supportRepository.save(support);

            auditService.createTransaction("CREATE", "CREACIÓN DE SOPORTE PARA IDONEIDAD", "RUI_SUPPORT",
                    support.getId(), null);
        } catch (Exception e) {
            log.error("Error saving support for idoneidad {}: {}", idoneidadId, e.getMessage());
            throw new BusinessException("Error saving support for idoneidad: " + e.getMessage());
        }
    }

    // Endpoinds para la creacion de intermediarios
    @Transactional
public RuiIntermediary create(RuiIntermediary intermediary, RuiUser currentUser) {
    try {
        log.info("Creando nuevo intermediario de tipo: {}", 
            intermediary.getTypeIntermediarieId() != null ? intermediary.getTypeIntermediarieId().getId() : "No especificado");
        
        // Validar que no exista un intermediario con el mismo número de documento o NIT
        boolean exists = false;
        if (intermediary.getPersonId() != null && intermediary.getPersonId().getDocumentNumber() != null) {
            exists = ruiPersonRepository.existsByDocumentNumber(intermediary.getPersonId().getDocumentNumber());
        } else if (intermediary.getCompanyId() != null && intermediary.getCompanyId().getNit() != null) {
            exists = intermediaryRepository.findByCompanyNit(intermediary.getCompanyId().getNit()).isPresent();
        }
        
        if (exists) {
            throw new BusinessException("Ya existe un intermediario con estos datos");
        }
        
        // Guardar primero las entidades dependientes
        if (intermediary.getPersonId() != null) {
            intermediary.setPersonId(ruiPersonRepository.save(intermediary.getPersonId()));
        }
        
        if (intermediary.getCompanyId() != null) {
            intermediary.setCompanyId(companyRepository.save(intermediary.getCompanyId()));
        }
        
        // Guardar el intermediario
        intermediary = intermediaryRepository.save(intermediary);
        
        // Crear historial usando servicios existentes
        RuiIntermediaryHistory history = new RuiIntermediaryHistory();
        history.setIntermediaryId(intermediary);
        history.setFunctionaryId(currentUser);
        history.setObservation("Creación inicial del registro");
        history.setDatetime(new Date());
        history.setStatus((short) intermediary.getState().getState().intValue());
        intermediaryHistoryRepository.save(history);
        
        // Crear asignación
        RuiAssignment assignment = new RuiAssignment();
        assignment.setIntermediaryId(intermediary);
        assignment.setUserId(currentUser);
        assignment.setStatus(1);
        assignment.setAssignmentDate(new Date());
        assignmentRepository.save(assignment);
        
        // Registrar auditoría
        auditService.createTransaction(
            "CREATE", 
            "CREACIÓN DE INTERMEDIARIO", 
            "RUI_INTERMEDIARIES", 
            intermediary.getId(), 
            currentUser
        );
        
        log.info("Intermediario creado con ID: {}", intermediary.getId());
        return intermediary;
    } catch (Exception e) {
        log.error("Error al crear intermediario: {}", e.getMessage(), e);
        throw new BusinessException("Error al crear intermediario: " + e.getMessage(), e);
    }
}

    private void validateUniqueDocument(RuiIntermediary intermediary) {
        // Validar que no exista un intermediario con el mismo número de documento o NIT
        if (intermediary.getPersonId() != null) {
            String documentNumber = intermediary.getPersonId().getDocumentNumber();
            if (documentNumber != null && !documentNumber.isEmpty()) {
                boolean exists = ruiPersonRepository.existsByDocumentNumber(documentNumber);
                if (exists) {
                    throw new BusinessException(
                            "Ya existe un intermediario con el número de documento: " + documentNumber);
                }
            }
        }

        if (intermediary.getCompanyId() != null) {
            String nit = intermediary.getCompanyId().getNit();
            if (nit != null && !nit.isEmpty()) {
                Optional<RuiIntermediary> existingIntermediary = intermediaryRepository.findByCompanyNit(nit);
                if (existingIntermediary.isPresent()) {
                    throw new BusinessException("Ya existe un intermediario con el NIT: " + nit);
                }
            }
        }
    }

    private void createAssignment(RuiIntermediary intermediary, RuiUser user) {
        RuiAssignment assignment = new RuiAssignment();
        assignment.setIntermediaryId(intermediary);
        assignment.setUserId(user);
        assignment.setStatus(1); // Activo
        assignment.setAssignmentDate(new Date());
        
        assignmentRepository.save(assignment);
    }
}
