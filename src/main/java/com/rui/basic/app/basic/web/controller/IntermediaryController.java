package com.rui.basic.app.basic.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rui.basic.app.basic.service.DocumentTypeService;
import com.rui.basic.app.basic.service.DocumentoService;
import com.rui.basic.app.basic.service.FileStorageService;
import com.rui.basic.app.basic.service.FirmaDigitalizadaService;
import com.rui.basic.app.basic.service.IdoneidadProfesionalService;
import com.rui.basic.app.basic.service.InfraestructuraHumanaService;
import com.rui.basic.app.basic.service.InfraestructuraOperativaService;
import com.rui.basic.app.basic.service.IntermediaryService;
import com.rui.basic.app.basic.service.IntermediaryTypeService;
import com.rui.basic.app.basic.service.UbicacionService;
import com.rui.basic.app.basic.service.UserService;
import com.rui.basic.app.basic.service.email.EmailService;
import com.rui.basic.app.basic.web.dto.CityDTO;
import com.rui.basic.app.basic.web.dto.DepartmentDTO;
import com.rui.basic.app.basic.web.dto.EmailTemplateDTO;
import com.rui.basic.app.basic.web.dto.ExperienciaLaboralDTO;
import com.rui.basic.app.basic.web.dto.FormFieldStateDTO;
import com.rui.basic.app.basic.web.dto.IdoneidadProfesionalDTO;
import com.rui.basic.app.basic.web.dto.InfrastructuraHumanaDTO;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;

import com.rui.basic.app.basic.domain.entities.RuiCity;
import com.rui.basic.app.basic.domain.entities.RuiCompany;
import com.rui.basic.app.basic.domain.entities.RuiDepartment;
import com.rui.basic.app.basic.domain.entities.RuiGenerics;
import com.rui.basic.app.basic.domain.entities.RuiHistoryDetails;
import com.rui.basic.app.basic.domain.entities.RuiIdoniedad;
import com.rui.basic.app.basic.domain.entities.RuiInfraHuman;
import com.rui.basic.app.basic.domain.entities.RuiInfraOperational;
import com.rui.basic.app.basic.domain.entities.RuiIntermediary;
import com.rui.basic.app.basic.domain.entities.RuiPerson;
import com.rui.basic.app.basic.domain.entities.RuiSupport;
import com.rui.basic.app.basic.domain.entities.RuiUser;
import com.rui.basic.app.basic.domain.entities.RuiWorkExperience;
import com.rui.basic.app.basic.domain.enums.IntermediaryState;
import com.rui.basic.app.basic.repository.RuiCityRepository;
import com.rui.basic.app.basic.repository.RuiDepartmentRepository;
import com.rui.basic.app.basic.repository.RuiHistoryDetailsRepository;
import com.rui.basic.app.basic.repository.RuiInfraHumanRepository;
import com.rui.basic.app.basic.repository.RuiInfraOperationalRepository;
import com.rui.basic.app.basic.repository.RuiIntermediaryRepository;
import com.rui.basic.app.basic.repository.RuiPersonRepository;
import com.rui.basic.app.basic.repository.RuiSupportRepository;
import com.rui.basic.app.basic.repository.RuiUserRepository;
import com.rui.basic.app.basic.repository.RuiWorkExperienceRepository;

@Controller
@RequestMapping("/intermediary")
public class IntermediaryController {

    private static final Logger log = LoggerFactory.getLogger(IntermediaryController.class);

    private final IntermediaryService intermediaryService;
    private final IdoneidadProfesionalService idoneidadService;
    private final EmailService emailService;
    private final FileStorageService fileStorageService;
    private final InfraestructuraHumanaService infraestructuraHumanaService;
    private final InfraestructuraOperativaService infraestructuraOperativaService;
    private final FirmaDigitalizadaService firmaDigitalizadaService;
    private final DocumentoService documentoService;
    private final UbicacionService ubicacionService;
    private final UserService userService; // Añadir UserService
    private final RuiHistoryDetailsRepository historyDetailsRepository;
    private final RuiUserRepository ruiUserRepository;
    private final RuiCityRepository ruiCityRepository;
    private final RuiDepartmentRepository ruiDepartmentRepository;
    @Autowired
    private RuiSupportRepository ruiSupportRepository;
    @Autowired
    private RuiInfraOperationalRepository infraOperativaRepository;
    @Autowired
    private RuiWorkExperienceRepository workExperienceRepository;
    @Autowired
    private RuiIntermediaryRepository ruiIntermediaryRepository;
    @Autowired
    private RuiPersonRepository ruiPersonRepository;
    @Autowired
    private RuiInfraHumanRepository infraHumanaRepository;
    @Autowired
    private IdoneidadProfesionalService idoniedadService;

    private final DocumentTypeService documentTypeService;
    private final IntermediaryTypeService intermediaryTypeService;

    // Inyectar app.documentos.ruta como un campo en el controlador
    @Value("${app.documentos.ruta}")
    private String attachmentsDir;

    public IntermediaryController(
            IntermediaryService intermediaryService,
            EmailService emailService,
            FileStorageService fileStorageService,
            IdoneidadProfesionalService idoneidadService,
            InfraestructuraHumanaService infraestructuraHumanaService,
            DocumentoService documentoService,
            InfraestructuraOperativaService infraestructuraOperativaService,
            FirmaDigitalizadaService firmaDigitalizadaService,
            UbicacionService ubicacionService,
            UserService userService, // Inyectar UserService
            RuiHistoryDetailsRepository historyDetailsRepository,
            RuiUserRepository ruiUserRepository,
            RuiCityRepository ruiCityRepository,
            RuiDepartmentRepository ruiDepartmentRepository,
            DocumentTypeService documentTypeService,
            IntermediaryTypeService intermediaryTypeService) {
        this.intermediaryService = intermediaryService;
        this.emailService = emailService;
        this.fileStorageService = fileStorageService;
        this.idoneidadService = idoneidadService;
        this.infraestructuraHumanaService = infraestructuraHumanaService;
        this.documentoService = documentoService;
        this.infraestructuraOperativaService = infraestructuraOperativaService;
        this.firmaDigitalizadaService = firmaDigitalizadaService;
        this.ubicacionService = ubicacionService;
        this.userService = userService; // Asignar UserService
        this.historyDetailsRepository = historyDetailsRepository;
        this.ruiUserRepository = ruiUserRepository;
        this.ruiCityRepository = ruiCityRepository;
        this.ruiDepartmentRepository = ruiDepartmentRepository;
        this.documentTypeService = documentTypeService;
        this.intermediaryTypeService = intermediaryTypeService;
    }

    @PostMapping("/status/update")
    public String updateStatus(
            @RequestParam Long id,
            @RequestParam String status) {
        try {
            // Convertir el status a IntermediaryState
            IntermediaryState state = IntermediaryState.fromState(Integer.parseInt(status));

            // Actualizar el estado
            intermediaryService.updateIntermediaryStatus(id, state, null, null);

            // Preparar y enviar el correo
            EmailTemplateDTO emailDTO = intermediaryService.prepareStatusEmail(id, state);
            if (emailDTO != null) {
                emailService.sendEmail(emailDTO);
            }

            return "redirect:/intermediary/list";
        } catch (Exception e) {
            log.error("Error updating intermediary status: {}", e.getMessage());
            return "redirect:/error";
        }
    }

    @PostMapping("/file/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam("intermediaryId") Long intermediaryId) {
        try {
            // Obtener el usuario actual (deberías implementar esto según tu sistema de
            // autenticación)
            Long currentUserId = getCurrentUserId();

            // Almacenar el archivo
            String filePath = fileStorageService.storeFile(
                    file,
                    String.format("intermediary/%d/%s", intermediaryId, type),
                    currentUserId,
                    intermediaryId);

            // Guardar la referencia del archivo
            intermediaryService.saveSupport(intermediaryId, filePath, type);

            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error uploading file: " + e.getMessage());
        }
    }

    @GetMapping("/review/{id}")
    public String showReview(@PathVariable Long id, Model model) {
        try {
            RuiIntermediary intermediary = intermediaryService.findById(id);

            // Asegúrate de que esto se está ejecutando:
            Map<String, FormFieldStateDTO> fieldStates = intermediaryService.getFieldStates(id);
            model.addAttribute("fieldStates", fieldStates);

            // Cargar datos principales
            model.addAttribute("intermediary", intermediary);

            // Determinar tipo de intermediario
            boolean isAgente = intermediary.getTypeIntermediarieId() != null &&
                    intermediary.getTypeIntermediarieId().getId() == 4L;
            model.addAttribute("isAgente", isAgente);

            model.addAttribute("idoneidadList", idoneidadService.findMostRecentByIntermediary(id));

            // Cargar ubicación
            loadLocationData(intermediary, model);

            // Cargar infraestructuras y firma
            loadInfrastructuraHumana(id, model);
            loadInfrastructuraOperativa(id, model);
            loadFirmaDigitalizada(id, model);

            // Estado de revisión
            model.addAttribute("estadoRevisado",
                    intermediary.getState() == IntermediaryState.REVIEWED);

            return "intermediary/review";

        } catch (EntityNotFoundException e) {
            log.error("Intermediario no encontrado: {}", id);
            return "redirect:/error";
        } catch (Exception e) {
            log.error("Error al cargar la revisión del intermediario: {}", e.getMessage(), e);
            return "redirect:/error";
        }
    }

    private void loadLocationData(RuiIntermediary intermediary, Model model) {
        Long departmentId = null;
        Long cityId = null;

        if (intermediary.getCompanyId() != null) {
            departmentId = Optional.ofNullable(intermediary.getCompanyId().getDepartmentId())
                    .map(RuiDepartment::getId)
                    .orElse(null);
            cityId = Optional.ofNullable(intermediary.getCompanyId().getCityId())
                    .map(RuiCity::getId)
                    .orElse(null);
        } else if (intermediary.getPersonId() != null) {
            departmentId = Optional.ofNullable(intermediary.getPersonId().getDepartmentId())
                    .map(RuiDepartment::getId)
                    .orElse(null);
            cityId = Optional.ofNullable(intermediary.getPersonId().getCityId())
                    .map(RuiCity::getId)
                    .orElse(null);
        }

        if (departmentId != null) {
            ubicacionService.getDepartmentById(departmentId)
                    .ifPresent(dept -> model.addAttribute("departamento", dept));
        }

        if (cityId != null) {
            ubicacionService.getCityById(cityId)
                    .ifPresent(city -> model.addAttribute("ciudad", city));
        }
    }

    private void loadInfrastructuraHumana(Long id, Model model) {
        try {
            InfrastructuraHumanaDTO infraestructuraHumana = infraestructuraHumanaService.findByIntermediary(id);
            model.addAttribute("infraestructuraHumana", infraestructuraHumana);

            /*
             * List<ExperienciaLaboralDTO> experiencias =
             * infraestructuraHumanaService.findWorkExperienceByIntermediary(id);
             * model.addAttribute("experienciasLaborales", experiencias);
             */
            if (infraestructuraHumana != null && infraestructuraHumana.getId() != null) {
                List<ExperienciaLaboralDTO> experiencias = infraestructuraHumanaService.findWorkExperienceByInfraHuman(
                        infraestructuraHumana.getId());
                model.addAttribute("experienciasLaborales", experiencias);
            } else {
                model.addAttribute("experienciasLaborales", new ArrayList<>());
            }
        } catch (Exception e) {
            log.warn("Error al cargar infraestructura humana: {}", e.getMessage());
            model.addAttribute("infraestructuraHumana", null);
            model.addAttribute("experienciasLaborales", new ArrayList<>());
        }
    }

    private void loadInfrastructuraOperativa(Long id, Model model) {
        try {
            model.addAttribute("infraestructuraOperativa",
                    infraestructuraOperativaService.findByIntermediary(id));
        } catch (Exception e) {
            log.warn("Error al cargar infraestructura operativa: {}", e.getMessage());
            model.addAttribute("infraestructuraOperativa", null);
        }
    }

    private void loadFirmaDigitalizada(Long id, Model model) {
        try {
            model.addAttribute("firmaDigitalizada",
                    firmaDigitalizadaService.findByIntermediary(id));
        } catch (Exception e) {
            log.warn("Error al cargar firma digitalizada: {}", e.getMessage());
            model.addAttribute("firmaDigitalizada", null);
        }
    }

    @GetMapping("/list")
    public String listIntermediaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String search,
            Model model) {

        try {
            Page<RuiIntermediary> intermediariesPage;

            if (StringUtils.hasText(search)) {
                intermediariesPage = intermediaryService.search(search, page, size, sort, direction);
                model.addAttribute("searchActive", true);
            } else {
                intermediariesPage = intermediaryService.findAll(page, size, sort, direction);
            }

            model.addAttribute("intermediaries", intermediariesPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", intermediariesPage.getTotalPages());
            model.addAttribute("totalItems", intermediariesPage.getTotalElements());
            model.addAttribute("sort", sort);
            model.addAttribute("direction", direction);
            model.addAttribute("search", search);
            model.addAttribute("activeTab", "intermediaryList");

            // Agregar mensaje cuando no hay resultados
            if (intermediariesPage.isEmpty() && StringUtils.hasText(search)) {
                model.addAttribute("noResults", true);
            }

            return "intermediary/list";
        } catch (Exception e) {
            model.addAttribute("error", "Error al buscar intermediarios: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/view/{id}")
    public String viewIntermediary(@PathVariable Long id, Model model) {
        try {
            RuiIntermediary intermediary = intermediaryService.findById(id);

            // Verificar permisos según el tipo de usuario...

            // Cargar datos principales
            model.addAttribute("intermediary", intermediary);

            // Determinar tipo de intermediario
            boolean isAgente = intermediary.getTypeIntermediarieId() != null &&
                    intermediary.getTypeIntermediarieId().getId() == 4L;
            model.addAttribute("isAgente", isAgente);

            // Cargar idoneidad con indicador de observaciones
            List<IdoneidadProfesionalDTO> idoneidadList = idoneidadService.findMostRecentByIntermediary(id);
            // Marcar si tienen observaciones
            for (IdoneidadProfesionalDTO dto : idoneidadList) {
                dto.setHasObservation(idoneidadService.hasObservation(dto.getId()));
            }
            model.addAttribute("idoneidadList", idoneidadList);

            // Cargar estados de campos para observaciones
            Map<String, FormFieldStateDTO> fieldStates = intermediaryService.getFieldStates(id);
            model.addAttribute("fieldStates", fieldStates);

            // Cargar ubicación
            loadLocationData(intermediary, model);

            // Cargar infraestructuras y firma
            loadInfrastructuraHumana(id, model);

            // Marcar si las experiencias laborales tienen observaciones
            if (model.getAttribute("experienciasLaborales") != null) {
                List<ExperienciaLaboralDTO> experiencias = (List<ExperienciaLaboralDTO>) model
                        .getAttribute("experienciasLaborales");
                for (ExperienciaLaboralDTO exp : experiencias) {
                    exp.setHasObservation(infraestructuraHumanaService.hasWorkExpObservation(exp.getId()));
                }
            }

            loadInfrastructuraOperativa(id, model);
            loadFirmaDigitalizada(id, model);

            return "intermediary/view";

        } catch (EntityNotFoundException e) {
            log.error("Intermediario no encontrado: {}", id);
            return "redirect:/error";
        } catch (Exception e) {
            log.error("Error al cargar los detalles del intermediario: {}", e.getMessage(), e);
            return "redirect:/error";
        }
    }

    @GetMapping("/review")
    public String reviewIntermediary() {
        return "intermediary/review";
    }

    @GetMapping("/workexperience/{id}/download")
    public ResponseEntity<Resource> downloadWorkExperienceDocument(@PathVariable Long id) {
        try {
            Resource file = documentoService.loadWorkExperienceDocument(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (Exception e) {
            log.error("Error al descargar documento de experiencia laboral: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/workexperience/{id}/view")
    public ResponseEntity<Resource> viewWorkExperienceDocument(@PathVariable Long id) {
        try {
            Resource file = documentoService.loadWorkExperienceDocument(id);

            // Determinar el tipo de contenido
            String contentType = "application/pdf";
            String filename = file.getFilename();
            if (filename != null) {
                if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (filename.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                }
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (Exception e) {
            log.error("Error al visualizar documento de experiencia laboral: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/firma/{id}")
    public ResponseEntity<Resource> verFirma(@PathVariable Long id) {
        try {
            Resource firma = firmaDigitalizadaService.loadFirmaDigitalizada(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // Ajustar según el formato real
                    .body(firma);
        } catch (Exception e) {
            log.error("Error al mostrar firma: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/status/marcar-revisado/{id}")
    public String marcarComoRevisado(@PathVariable Long id, RedirectAttributes redirectAttributes, Model model) {
        Map<String, Object> result = firmaDigitalizadaService.marcarComoRevisado(id);

        if ((Boolean) result.get("success")) {
            redirectAttributes.addFlashAttribute("mensaje", result.get("message"));

            // Si hay observaciones pendientes, agregar información adicional
            if (result.containsKey("observacionesPendientes") && (Boolean) result.get("observacionesPendientes")) {
                redirectAttributes.addFlashAttribute("advertencia",
                        "Se ha marcado como revisado, pero existen " + result.get("cantidadObservaciones") +
                                " observaciones pendientes.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", result.get("message"));
        }

        return "redirect:/intermediary/review/" + id;
    }

    @GetMapping("/check-observaciones/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verificarObservaciones(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long cantidadObservaciones = historyDetailsRepository.countByIntermediaryId(id);
            response.put("observacionesPendientes", cantidadObservaciones > 0);
            response.put("cantidadObservaciones", cantidadObservaciones);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al verificar observaciones: {}", e.getMessage(), e);
            response.put("error", "Error al verificar observaciones");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/status/enviar-complementar/{id}")
    public String enviarAComplementar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean success = firmaDigitalizadaService.enviarAComplementar(id);

        if (success) {
            redirectAttributes.addFlashAttribute("mensaje", "Registro enviado a complementar correctamente");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error al enviar a complementar");
        }

        return "redirect:/intermediary/review/" + id;
    }

    @PostMapping("/status/aprobar/{id}")
    public String aprobarRegistro(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean success = firmaDigitalizadaService.aprobarRegistro(id);

        if (success) {
            redirectAttributes.addFlashAttribute("mensaje", "Registro aprobado correctamente");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error al aprobar el registro");
        }

        return "redirect:/intermediary/review/" + id;
    }

    @PostMapping("/status/regresar-funcionario/{id}")
    public String regresarAFuncionario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean success = firmaDigitalizadaService.regresarAFuncionario(id);

        if (success) {
            redirectAttributes.addFlashAttribute("mensaje", "Registro regresado a funcionario correctamente");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error al regresar a funcionario");
        }

        return "redirect:/intermediary/review/" + id;
    }

    // endpoint para documentos de tab infraestructura operativa
    @GetMapping("/infraoperativa/soporte/{tipo}/{id}")
    public ResponseEntity<Resource> downloadInfraOperativaDocument(
            @PathVariable String tipo,
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean download) {
        try {
            Resource file = documentoService.loadInfraOperativaDocument(id, tipo);

            ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();

            if (download) {
                // Configurar para descarga
                responseBuilder.header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"");
            } else {
                // Configurar para visualización en línea
                responseBuilder.header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getFilename() + "\"");
                // Determinar tipo de contenido
                String contentType = "application/pdf"; // Por defecto
                String filename = file.getFilename();
                if (filename != null) {
                    if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                        contentType = "image/jpeg";
                    } else if (filename.toLowerCase().endsWith(".png")) {
                        contentType = "image/png";
                    }
                }
                responseBuilder.contentType(MediaType.parseMediaType(contentType));
            }

            return responseBuilder.body(file);
        } catch (Exception e) {
            log.error("Error al descargar documento de infraestructura operativa: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/infraoperativa/soporte/{tipo}/{id}/view")
    public ResponseEntity<Resource> viewInfraOperativaDocument(
            @PathVariable String tipo,
            @PathVariable Long id) {
        try {
            Resource file = documentoService.loadInfraOperativaDocument(id, tipo);

            // Determinar el tipo de contenido
            String contentType = "application/pdf";
            String filename = file.getFilename();
            if (filename != null) {
                if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (filename.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                }
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (Exception e) {
            log.error("Error al visualizar documento de infraestructura operativa: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    // endpoint para observaciones de tab infraestructura operativa
    @GetMapping("/workexp/{id}/observation")
    @ResponseBody
    public ResponseEntity<FormFieldStateDTO> getWorkExpObservation(@PathVariable Long id) {
        try {
            log.debug("Recibiendo solicitud GET para observación de experiencia laboral ID: {}", id);
            FormFieldStateDTO state = infraestructuraHumanaService.getWorkExpObservation(id);
            return ResponseEntity.ok(state);
        } catch (EntityNotFoundException e) {
            log.error("Experiencia laboral no encontrada: {}", id);
            FormFieldStateDTO state = new FormFieldStateDTO();
            state.setIconClose(false);
            state.setCommentDisabled(true);
            state.setObservation("Experiencia laboral no encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(state);
        } catch (Exception e) {
            log.error("Error al obtener observación de experiencia laboral: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new FormFieldStateDTO());
        }
    }

    @PostMapping("/workexp/{id}/observation")
    @ResponseBody
    public ResponseEntity<FormFieldStateDTO> createWorkExpObservation(@PathVariable Long id) {
        try {
            log.debug("Recibiendo solicitud POST para crear observación en experiencia laboral ID: {}", id);
            FormFieldStateDTO state = infraestructuraHumanaService.createWorkExpObservation(id);
            return ResponseEntity.ok(state);
        } catch (EntityNotFoundException e) {
            log.error("Experiencia laboral no encontrada: {}", id);
            FormFieldStateDTO state = new FormFieldStateDTO();
            state.setIconClose(false);
            state.setCommentDisabled(true);
            state.setObservation("Experiencia laboral no encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(state);
        } catch (Exception e) {
            log.error("Error al crear observación en experiencia laboral: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new FormFieldStateDTO());
        }
    }

    @DeleteMapping("/workexp/{id}/observation")
    @ResponseBody
    public ResponseEntity<FormFieldStateDTO> removeWorkExpObservation(@PathVariable Long id) {
        try {
            log.debug("Recibiendo solicitud DELETE para eliminar observación en experiencia laboral ID: {}", id);
            FormFieldStateDTO state = infraestructuraHumanaService.removeWorkExpObservation(id);
            return ResponseEntity.ok(state);
        } catch (EntityNotFoundException e) {
            log.error("Experiencia laboral no encontrada: {}", id);
            FormFieldStateDTO state = new FormFieldStateDTO();
            state.setIconClose(false);
            state.setCommentDisabled(true);
            state.setObservation("Experiencia laboral no encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(state);
        } catch (Exception e) {
            log.error("Error al eliminar observación en experiencia laboral: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new FormFieldStateDTO());
        }
    }

    @PutMapping("/workexp/{id}/observation")
    @ResponseBody
    public ResponseEntity<FormFieldStateDTO> updateWorkExpObservation(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            String observation = payload != null ? payload.get("observation") : null;
            if (observation == null) {
                throw new IllegalArgumentException("La observación no puede ser nula");
            }

            log.debug("Recibiendo solicitud PUT para actualizar observación en experiencia laboral ID: {}", id);
            FormFieldStateDTO state = infraestructuraHumanaService.updateWorkExpObservation(id, observation);
            return ResponseEntity.ok(state);
        } catch (IllegalArgumentException e) {
            log.error("Parámetros inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new FormFieldStateDTO());
        } catch (EntityNotFoundException e) {
            log.error("Experiencia laboral no encontrada: {}", id);
            FormFieldStateDTO state = new FormFieldStateDTO();
            state.setIconClose(false);
            state.setCommentDisabled(true);
            state.setObservation("Experiencia laboral no encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(state);
        } catch (Exception e) {
            log.error("Error al actualizar observación en experiencia laboral: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new FormFieldStateDTO());
        }
    }

    // --------------------------------------------------------------------------------
    // Otros métodos del controlador
    @GetMapping("/my-registries")
    public String listUserRegistries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.error("No hay usuario autenticado");
                return "redirect:/auth/login";
            }
            String username = authentication.getName();
            log.debug("Usuario autenticado: {}", username);

            Page<RuiIntermediary> userRegistries = intermediaryService.findByUser(username, page, size, sort,
                    direction);

            model.addAttribute("registries", userRegistries.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", userRegistries.getTotalPages());
            model.addAttribute("totalItems", userRegistries.getTotalElements());
            model.addAttribute("sort", sort);
            model.addAttribute("direction", direction);
            model.addAttribute("activeTab", "myRegistries");

            if (userRegistries.isEmpty()) {
                model.addAttribute("noResults", true);
            }

            return "intermediary/my-registries";
        } catch (Exception e) {
            log.error("Error al listar registros del usuario: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar los registros: " + e.getMessage());
            return "error";
        }
    }

    // Método auxiliar para obtener el ID del usuario actual (mejorado)
    /*
     * private Long getCurrentUserId() {
     * Authentication authentication =
     * SecurityContextHolder.getContext().getAuthentication();
     * if (authentication != null && authentication.isAuthenticated()) {
     * // Asumimos que el ID del usuario está en los detalles del Authentication o
     * como principal
     * // Debes ajustarlo según tu implementación de autenticación
     * return 1L; // Temporal, reemplaza con la lógica real
     * }
     * throw new IllegalStateException("No hay usuario autenticado");
     * }
     */

    // --------------------------------------------------------------------------------
    // métodos para editar

    @PostMapping("/complement/{id}")
    public String complementIntermediary(
            @PathVariable Long id,
            @ModelAttribute RuiIntermediary intermediary,
            @RequestParam(required = false) MultipartFile ccFile,
            @RequestParam(required = false) MultipartFile softFile,
            @RequestParam(required = false) MultipartFile hardFile,
            @RequestParam(required = false) MultipartFile signatureFile,
            @RequestParam Map<String, String> allParams,
            MultipartHttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            log.debug("Inicio de complementIntermediary para ID: {}", id);

            // Depuración detallada
            log.debug("Request Content-Type: {}", request.getContentType());
            log.debug("Todos los parámetros de formulario: {}", allParams);
            log.debug("Todos los archivos recibidos en request.getFileMap():");
            request.getFileMap().forEach((name, file) -> log.debug("Archivo: name={}, originalFilename={}, size={}",
                    name, file.getOriginalFilename(), file.getSize()));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.error("No hay usuario autenticado");
                return "redirect:/auth/login";
            }
            String username = authentication.getName();
            Long currentUserId = getCurrentUserId();

            RuiIntermediary existingIntermediary = intermediaryService.findByIdWithDetails(id);
            if (!isUserAuthorizedForIntermediary(existingIntermediary, username)) {
                redirectAttributes.addFlashAttribute("error", "No está autorizado para editar este registro");
                return "redirect:/intermediary/my-registries";
            }

            // 1. Actualizar Información General
            updateIntermediaryDetails(existingIntermediary, intermediary);

            // 2. Actualizar Idoneidad Profesional
            List<RuiIdoniedad> existingIdoneidad = existingIntermediary.getRuiIdoniedadList();
            List<Long> idoneidadIds = new ArrayList<>();
            for (int i = 0; i < existingIdoneidad.size(); i++) {
                String idKey = "idoneidadIds[" + i + "]";
                if (allParams.containsKey(idKey)) {
                    idoneidadIds.add(Long.parseLong(allParams.get(idKey)));
                }
            }
            for (int i = 0; i < idoneidadIds.size(); i++) {
                Long idoneidadId = idoneidadIds.get(i);
                RuiIdoniedad idoniedad = existingIdoneidad.stream()
                        .filter(idon -> idon.getId().equals(idoneidadId))
                        .findFirst().orElse(null);
                if (idoniedad != null) {
                    idoniedad.setCourseEntity(allParams.get("idoneidadCourseEntities[" + i + "]"));
                    String dateStr = allParams.get("idoneidadDateCourses[" + i + "]");
                    if (dateStr != null && !dateStr.isEmpty()) {
                        idoniedad.setDateCourse(new SimpleDateFormat("yyyy-MM-dd").parse(dateStr));
                    }
                    RuiPerson person = idoniedad.getPersonId();
                    if (person != null) {
                        person.setDocumentType(
                                allParams.getOrDefault("idoneidadDocumentTypes[" + i + "]", person.getDocumentType()));
                        person.setDocumentNumber(allParams.getOrDefault("idoneidadDocumentNumbers[" + i + "]",
                                person.getDocumentNumber()));
                        person.setFirstName(
                                allParams.getOrDefault("idoneidadFirstNames[" + i + "]", person.getFirstName()));
                        person.setSecondName(
                                allParams.getOrDefault("idoneidadSecondNames[" + i + "]", person.getSecondName()));
                        person.setFirstSurname(
                                allParams.getOrDefault("idoneidadFirstSurnames[" + i + "]", person.getFirstSurname()));
                        person.setSecondSurname(allParams.getOrDefault("idoneidadSecondSurnames[" + i + "]",
                                person.getSecondSurname()));
                        ruiPersonRepository.save(person);
                    }
                    idoniedadService.saveIdoneidad(idoniedad);

                    MultipartFile idoneidadFile = request.getFile("idoneidadFiles[" + i + "]");
                    if (idoneidadFile != null && !idoneidadFile.isEmpty()) {
                        String filePath = fileStorageService.storeFile(idoneidadFile, "IDONIEDAD/" + idoneidadId,
                                currentUserId, id, Paths.get(attachmentsDir));
                        ruiSupportRepository.findByIdoniedadId(idoneidadId).ifPresent(support -> {
                            support.setStatus((short) 0);
                            ruiSupportRepository.save(support);
                        });
                        RuiSupport support = new RuiSupport();
                        support.setIdoniedadId(idoniedad);
                        support.setFilename(FilenameUtils.getName(filePath));
                        support.setRoute(FilenameUtils.getFullPathNoEndSeparator(filePath));
                        support.setExtencion(FilenameUtils.getExtension(filePath));
                        support.setStatus((short) 1);
                        ruiSupportRepository.save(support);
                        log.info("Archivo de idoneidad guardado para ID {}: {}", idoneidadId, filePath);
                    } else {
                        log.debug("No se proporcionó archivo para idoneidad ID: {} en índice: {}", idoneidadId, i);
                    }
                }
            }

            // 3. Actualizar Infraestructura Humana
            RuiInfraHuman infraHuman = existingIntermediary.getInfrastructureHumanId();
            if (infraHuman != null && intermediary.getInfrastructureHumanId() != null) {
                RuiPerson lawyer = infraHuman.getLawyerId();
                if (lawyer != null) {
                    lawyer.setDocumentType(intermediary.getInfrastructureHumanId().getLawyerId().getDocumentType());
                    lawyer.setDocumentNumber(intermediary.getInfrastructureHumanId().getLawyerId().getDocumentNumber());
                    lawyer.setFirstName(intermediary.getInfrastructureHumanId().getLawyerId().getFirstName());
                    lawyer.setSecondName(intermediary.getInfrastructureHumanId().getLawyerId().getSecondName());
                    lawyer.setFirstSurname(intermediary.getInfrastructureHumanId().getLawyerId().getFirstSurname());
                    lawyer.setSecondSurname(intermediary.getInfrastructureHumanId().getLawyerId().getSecondSurname());
                    ruiPersonRepository.save(lawyer);
                }

                Set<RuiWorkExperience> existingExp = infraHuman.getWorkExperiences();
                List<Long> workExpIds = new ArrayList<>();
                for (int i = 0; i < existingExp.size(); i++) {
                    String idKey = "workExperienceIds[" + i + "]";
                    if (allParams.containsKey(idKey)) {
                        workExpIds.add(Long.parseLong(allParams.get(idKey)));
                    }
                }
                int index = 0;
                for (RuiWorkExperience exp : existingExp) {
                    if (index < workExpIds.size() && exp.getId().equals(workExpIds.get(index))) {
                        exp.setCompany(allParams.get("workExperienceCompanies[" + index + "]"));
                        exp.setCharge(allParams.get("workExperienceCharges[" + index + "]"));
                        exp.setNameBoss(allParams.get("workExperienceNameBosses[" + index + "]"));
                        exp.setPhoneBoss(allParams.get("workExperiencePhoneBosses[" + index + "]"));
                        String startDateStr = allParams.get("workExperienceStartDates[" + index + "]");
                        String endDateStr = allParams.get("workExperienceEndDates[" + index + "]");
                        if (startDateStr != null && !startDateStr.isEmpty()) {
                            exp.setStartDate(new SimpleDateFormat("yyyy-MM-dd").parse(startDateStr));
                        }
                        if (endDateStr != null && !endDateStr.isEmpty()) {
                            exp.setEndDate(new SimpleDateFormat("yyyy-MM-dd").parse(endDateStr));
                        }
                        workExperienceRepository.save(exp);

                        MultipartFile workExperienceFile = request.getFile("workExperienceFiles[" + index + "]");
                        if (workExperienceFile != null && !workExperienceFile.isEmpty()) {
                            String filePath = fileStorageService.storeFile(workExperienceFile,
                                    "WORK_EXPERIENCE/" + exp.getId(), currentUserId, id, Paths.get(attachmentsDir));
                            ruiSupportRepository.findFirstByWorkExperienceId(exp).ifPresent(support -> {
                                support.setStatus((short) 0);
                                ruiSupportRepository.save(support);
                            });
                            RuiSupport support = new RuiSupport();
                            support.setWorkExperienceId(exp);
                            support.setFilename(FilenameUtils.getName(filePath));
                            support.setRoute(FilenameUtils.getFullPathNoEndSeparator(filePath));
                            support.setExtencion(FilenameUtils.getExtension(filePath));
                            support.setStatus((short) 1);
                            ruiSupportRepository.save(support);
                            log.info("Archivo de experiencia laboral guardado para ID {}: {}", exp.getId(), filePath);
                        } else {
                            log.debug("No se proporcionó archivo para experiencia laboral ID: {} en índice: {}",
                                    exp.getId(), index);
                        }
                        index++;
                    }
                }
                infraHumanaRepository.save(infraHuman);
            }

            // 4. Actualizar Infraestructura Operativa
            RuiInfraOperational infraOp = existingIntermediary.getInfrastructureOperationalId();
            if (infraOp != null && intermediary.getInfrastructureOperationalId() != null) {
                infraOp.setPhone1(intermediary.getInfrastructureOperationalId().getPhone1());
                infraOp.setPhone2(intermediary.getInfrastructureOperationalId().getPhone2());
                infraOp.setPhone3(intermediary.getInfrastructureOperationalId().getPhone3());
                infraOp.setPhoneFax(intermediary.getInfrastructureOperationalId().getPhoneFax());
                infraOp.setEmail(intermediary.getInfrastructureOperationalId().getEmail());
                infraOp.setAddressServiceOffice(
                        intermediary.getInfrastructureOperationalId().getAddressServiceOffice());
                infraOperativaRepository.save(infraOp);

                // ccFile
                if (ccFile != null && !ccFile.isEmpty()) {
                    String filePath = fileStorageService.storeFile(ccFile, "INFRA_OPERACIONAL/CC", currentUserId, id,
                            Paths.get(attachmentsDir));
                    List<RuiSupport> existingCcSupports = ruiSupportRepository.findAllByInfraOperationalCc(infraOp);
                    for (RuiSupport support : existingCcSupports) {
                        if (support.getStatus() == 1) {
                            support.setStatus((short) 0);
                            ruiSupportRepository.save(support);
                        }
                    }
                    RuiSupport support = new RuiSupport();
                    support.setInfraOperationalCc(infraOp);
                    support.setFilename(FilenameUtils.getName(filePath));
                    support.setRoute(FilenameUtils.getFullPathNoEndSeparator(filePath));
                    support.setExtencion(FilenameUtils.getExtension(filePath));
                    support.setStatus((short) 1);
                    ruiSupportRepository.save(support);
                    log.info("Archivo ccFile guardado: {}", filePath);
                }

                // softFile
                if (softFile != null && !softFile.isEmpty()) {
                    String filePath = fileStorageService.storeFile(softFile, "INFRA_OPERACIONAL/SOFT", currentUserId,
                            id, Paths.get(attachmentsDir));
                    List<RuiSupport> existingSoftSupports = ruiSupportRepository.findAllByInfraOperationalSoft(infraOp);
                    for (RuiSupport support : existingSoftSupports) {
                        if (support.getStatus() == 1) {
                            support.setStatus((short) 0);
                            ruiSupportRepository.save(support);
                        }
                    }
                    RuiSupport support = new RuiSupport();
                    support.setInfraOperationalSoft(infraOp);
                    support.setFilename(FilenameUtils.getName(filePath));
                    support.setRoute(FilenameUtils.getFullPathNoEndSeparator(filePath));
                    support.setExtencion(FilenameUtils.getExtension(filePath));
                    support.setStatus((short) 1);
                    ruiSupportRepository.save(support);
                    log.info("Archivo softFile guardado: {}", filePath);
                }

                // hardFile
                if (hardFile != null && !hardFile.isEmpty()) {
                    String filePath = fileStorageService.storeFile(hardFile, "INFRA_OPERACIONAL/HARD", currentUserId,
                            id, Paths.get(attachmentsDir));
                    List<RuiSupport> existingHardSupports = ruiSupportRepository.findAllByInfraOperationalHard(infraOp);
                    for (RuiSupport support : existingHardSupports) {
                        if (support.getStatus() == 1) {
                            support.setStatus((short) 0);
                            ruiSupportRepository.save(support);
                        }
                    }
                    RuiSupport support = new RuiSupport();
                    support.setInfraOperationalHard(infraOp);
                    support.setFilename(FilenameUtils.getName(filePath));
                    support.setRoute(FilenameUtils.getFullPathNoEndSeparator(filePath));
                    support.setExtencion(FilenameUtils.getExtension(filePath));
                    support.setStatus((short) 1);
                    ruiSupportRepository.save(support);
                    log.info("Archivo hardFile guardado: {}", filePath);
                }
            }

            // 5. Guardar Firma Digitalizada
            if (signatureFile != null && !signatureFile.isEmpty()) {
                String filePath = fileStorageService.storeFile(signatureFile, "FIRMA_DIGITALIZADA", currentUserId, id,
                        Paths.get(attachmentsDir));
                // Buscar soporte existente con status=1 o crear uno nuevo
                RuiSupport support = ruiSupportRepository.findByInfraOperationalSignAndStatus(
                        existingIntermediary.getInfrastructureOperationalId(), (short) 1)
                        .orElse(new RuiSupport());
                support.setInfraOperationalSign(existingIntermediary.getInfrastructureOperationalId());
                support.setFilename(FilenameUtils.getName(filePath));
                support.setRoute(FilenameUtils.getFullPathNoEndSeparator(filePath));
                support.setExtencion(FilenameUtils.getExtension(filePath));
                support.setStatus((short) 1);
                ruiSupportRepository.save(support);
                log.info("Archivo signatureFile guardado: {}", filePath);
            }

            intermediaryService.updateIntermediaryStatus(id, IntermediaryState.COMPLEMENTED,
                    "Información complementada", null);
            ruiIntermediaryRepository.save(existingIntermediary);

            redirectAttributes.addFlashAttribute("mensaje", "Registro complementado correctamente");
            return "redirect:/intermediary/my-registries";
        } catch (MaxUploadSizeExceededException e) {
            log.error("Tamaño de carga excedido: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "El tamaño total de los archivos excede el límite permitido. Por favor, suba archivos más pequeños.");
            return "redirect:/intermediary/complement/" + id;
        } catch (Exception e) {
            log.error("Error al complementar el registro: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al complementar el registro: " + e.getMessage());
            return "redirect:/intermediary/complement/" + id;
        }
    }

    /**
     * Método auxiliar para actualizar los detalles del intermediario
     */
    private void updateIntermediaryDetails(RuiIntermediary existing, RuiIntermediary updated) {
        // Actualizar datos de la compañía
        if (existing.getCompanyId() != null && updated.getCompanyId() != null) {
            RuiCompany company = existing.getCompanyId();
            company.setNit(updated.getCompanyId().getNit());
            company.setName(updated.getCompanyId().getName());

            // Asegurar que las entidades de departamento y ciudad son las correctas
            if (updated.getCompanyId().getDepartmentId() != null
                    && updated.getCompanyId().getDepartmentId().getId() != null) {
                Long deptId = updated.getCompanyId().getDepartmentId().getId();
                Optional<RuiDepartment> department = ruiDepartmentRepository.findById(deptId);
                department.ifPresent(company::setDepartmentId);
            }

            if (updated.getCompanyId().getCityId() != null && updated.getCompanyId().getCityId().getId() != null) {
                Long cityId = updated.getCompanyId().getCityId().getId();
                Optional<RuiCity> city = ruiCityRepository.findById(cityId);
                city.ifPresent(company::setCityId);
            }

            company.setAddress(updated.getCompanyId().getAddress());
            company.setEmail(updated.getCompanyId().getEmail());
            company.setPhone(updated.getCompanyId().getPhone());
        }
        // Actualizar datos de persona
        else if (existing.getPersonId() != null && updated.getPersonId() != null) {
            RuiPerson person = existing.getPersonId();
            person.setDocumentType(updated.getPersonId().getDocumentType());
            person.setDocumentNumber(updated.getPersonId().getDocumentNumber());
            person.setFirstName(updated.getPersonId().getFirstName());
            person.setSecondName(updated.getPersonId().getSecondName());
            person.setFirstSurname(updated.getPersonId().getFirstSurname());
            person.setSecondSurname(updated.getPersonId().getSecondSurname());

            // Asegurar que las entidades de departamento y ciudad son las correctas
            if (updated.getPersonId().getDepartmentId() != null
                    && updated.getPersonId().getDepartmentId().getId() != null) {
                Long deptId = updated.getPersonId().getDepartmentId().getId();
                Optional<RuiDepartment> department = ruiDepartmentRepository.findById(deptId);
                department.ifPresent(person::setDepartmentId);
            }

            if (updated.getPersonId().getCityId() != null && updated.getPersonId().getCityId().getId() != null) {
                Long cityId = updated.getPersonId().getCityId().getId();
                Optional<RuiCity> city = ruiCityRepository.findById(cityId);
                city.ifPresent(person::setCityId);
            }

            person.setAddress(updated.getPersonId().getAddress());
            person.setEmail(updated.getPersonId().getEmail());
            person.setPhone(updated.getPersonId().getPhone());
            person.setCellphone(updated.getPersonId().getCellphone());
        }

        // Actualizar infraestructura humana (abogado y experiencias laborales)
        if (existing.getInfrastructureHumanId() != null && updated.getInfrastructureHumanId() != null) {
            RuiInfraHuman infraHuman = existing.getInfrastructureHumanId();

            // Actualizar datos del abogado
            if (infraHuman.getLawyerId() != null && updated.getInfrastructureHumanId().getLawyerId() != null) {
                RuiPerson lawyer = infraHuman.getLawyerId();
                lawyer.setDocumentType(updated.getInfrastructureHumanId().getLawyerId().getDocumentType());
                lawyer.setDocumentNumber(updated.getInfrastructureHumanId().getLawyerId().getDocumentNumber());
                lawyer.setFirstName(updated.getInfrastructureHumanId().getLawyerId().getFirstName());
                lawyer.setSecondName(updated.getInfrastructureHumanId().getLawyerId().getSecondName());
                lawyer.setFirstSurname(updated.getInfrastructureHumanId().getLawyerId().getFirstSurname());
                lawyer.setSecondSurname(updated.getInfrastructureHumanId().getLawyerId().getSecondSurname());
            }

            // Actualizar experiencias laborales
            Set<RuiWorkExperience> existingExpSet = infraHuman.getWorkExperiences();
            Set<RuiWorkExperience> updatedExpSet = updated.getInfrastructureHumanId().getWorkExperiences();

            if (existingExpSet != null && updatedExpSet != null) {
                // Convertir Sets a Listas para poder iterar con índices
                List<RuiWorkExperience> existingExp = new ArrayList<>(existingExpSet);
                List<RuiWorkExperience> updatedExp = new ArrayList<>(updatedExpSet);

                // Actualizar solo los elementos que coincidan por ID
                for (RuiWorkExperience updatedExpItem : updatedExp) {
                    if (updatedExpItem.getId() != null) {
                        for (RuiWorkExperience existingExpItem : existingExp) {
                            if (existingExpItem.getId() != null
                                    && existingExpItem.getId().equals(updatedExpItem.getId())) {
                                existingExpItem.setCompany(updatedExpItem.getCompany());
                                existingExpItem.setCharge(updatedExpItem.getCharge());
                                existingExpItem.setNameBoss(updatedExpItem.getNameBoss());
                                existingExpItem.setPhoneBoss(updatedExpItem.getPhoneBoss());
                                existingExpItem.setStartDate(updatedExpItem.getStartDate());
                                existingExpItem.setEndDate(updatedExpItem.getEndDate());
                                break; // Salir del bucle interno una vez encontrado el match
                            }
                        }
                    }
                }
            }
        }

        // Actualizar infraestructura operativa
        if (existing.getInfrastructureOperationalId() != null && updated.getInfrastructureOperationalId() != null) {
            RuiInfraOperational infraOp = existing.getInfrastructureOperationalId();
            infraOp.setPhone1(updated.getInfrastructureOperationalId().getPhone1());
            infraOp.setPhone2(updated.getInfrastructureOperationalId().getPhone2());
            infraOp.setPhone3(updated.getInfrastructureOperationalId().getPhone3());
            infraOp.setPhoneFax(updated.getInfrastructureOperationalId().getPhoneFax());
            infraOp.setEmail(updated.getInfrastructureOperationalId().getEmail());
            infraOp.setAddressServiceOffice(updated.getInfrastructureOperationalId().getAddressServiceOffice());
        }
    }

    /**
     * Método auxiliar para verificar si el usuario está autorizado para el
     * intermediario
     */
    private boolean isUserAuthorizedForIntermediary(RuiIntermediary intermediary, String username) {
        // Verificar si el usuario es un administrador o tiene rol adecuado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return true;
        }

        // Obtener el usuario por nombre de usuario
        Optional<RuiUser> userOpt = ruiUserRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }

        RuiUser user = userOpt.get();

        // Verificar si el intermediario está asociado al usuario
        if (intermediary.getPersonId() != null && user.getPerson() != null) {
            return intermediary.getPersonId().getId().equals(user.getPerson().getId());
        } else if (intermediary.getCompanyId() != null && user.getCompany() != null) {
            return intermediary.getCompanyId().getId().equals(user.getCompany().getId());
        }

        return false;
    }

    /**
     * Método auxiliar para obtener el ID del usuario actual
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<RuiUser> userOpt = ruiUserRepository.findByUsername(username);
            return userOpt.map(RuiUser::getId).orElse(1L); // Devuelve 1 por defecto si no encuentra el usuario
        }
        return 1L; // Valor por defecto temporal
    }

    @GetMapping("/complement/{id}")
    public String showComplementForm(@PathVariable Long id, Model model) {
        try {
            // Obtener intermediario con todos sus detalles
            RuiIntermediary intermediary = intermediaryService.findByIdWithDetails(id);
            if (intermediary == null) {
                log.error("Intermediary con ID {} no encontrado", id);
                return "redirect:/error";
            }

            log.info("Intermediary encontrado: ID={}, TypeIntermediarieId={}",
                    intermediary.getId(),
                    intermediary.getTypeIntermediarieId() != null ? intermediary.getTypeIntermediarieId().getValue()
                            : "null");

            // Determinar si es agente por el tipo de intermediario
            boolean isAgente = intermediary.getTypeIntermediarieId() != null
                    && intermediary.getTypeIntermediarieId().getId() == 4L;

            // Inicializar objetos del tab de información general para evitar
            // NullPointerException
            if (isAgente) {
                // Si es agente, asegurar que personId está inicializado
                if (intermediary.getPersonId() == null) {
                    intermediary.setPersonId(new RuiPerson());
                    log.info("Inicializando personId para intermediario agente");
                } else {
                    Hibernate.initialize(intermediary.getPersonId());

                    // Inicializar propiedades anidadas de Person
                    if (intermediary.getPersonId().getDepartmentId() == null) {
                        intermediary.getPersonId().setDepartmentId(new RuiDepartment());
                    } else {
                        Hibernate.initialize(intermediary.getPersonId().getDepartmentId());
                    }

                    if (intermediary.getPersonId().getCityId() == null) {
                        intermediary.getPersonId().setCityId(new RuiCity());
                    } else {
                        Hibernate.initialize(intermediary.getPersonId().getCityId());
                    }
                }
            } else {
                // Si no es agente, asegurar que companyId está inicializado
                if (intermediary.getCompanyId() == null) {
                    intermediary.setCompanyId(new RuiCompany());
                    log.info("Inicializando companyId para intermediario empresa");
                } else {
                    Hibernate.initialize(intermediary.getCompanyId());

                    // Inicializar propiedades anidadas de Company
                    if (intermediary.getCompanyId().getDepartmentId() == null) {
                        intermediary.getCompanyId().setDepartmentId(new RuiDepartment());
                    } else {
                        Hibernate.initialize(intermediary.getCompanyId().getDepartmentId());
                    }

                    if (intermediary.getCompanyId().getCityId() == null) {
                        intermediary.getCompanyId().setCityId(new RuiCity());
                    } else {
                        Hibernate.initialize(intermediary.getCompanyId().getCityId());
                    }
                }
            }

            // Inicializar relaciones para evitar proxies no inicializados
            if (intermediary.getInfrastructureHumanId() != null) {
                Hibernate.initialize(intermediary.getInfrastructureHumanId());
                if (intermediary.getInfrastructureHumanId().getLawyerId() != null) {
                    Hibernate.initialize(intermediary.getInfrastructureHumanId().getLawyerId());
                    if (intermediary.getInfrastructureHumanId().getLawyerId().getDocumentType() != null) {
                        Hibernate.initialize(intermediary.getInfrastructureHumanId().getLawyerId().getDocumentType());
                    }
                }

                // Inicializar workExperiences si es null
                if (intermediary.getInfrastructureHumanId().getWorkExperiences() == null) {
                    intermediary.getInfrastructureHumanId().setWorkExperiences(new HashSet<>());
                } else {
                    Hibernate.initialize(intermediary.getInfrastructureHumanId().getWorkExperiences());
                }
            } else if (!isAgente) {
                // Si no es agente y no tiene infraestructura humana, inicializarla
                intermediary.setInfrastructureHumanId(new RuiInfraHuman());
                intermediary.getInfrastructureHumanId().setWorkExperiences(new HashSet<>());
                log.info("Inicializando infrastructureHumanId para intermediario empresa");
            }

            // Inicializar infraestructuraOperacional si es null
            if (intermediary.getInfrastructureOperationalId() == null) {
                intermediary.setInfrastructureOperationalId(new RuiInfraOperational());
                log.info("Inicializando infrastructureOperationalId");
            } else {
                Hibernate.initialize(intermediary.getInfrastructureOperationalId());
            }

            // Añadir atributos al modelo
            model.addAttribute("intermediary", intermediary);
            model.addAttribute("isAgente", isAgente);
            model.addAttribute("documentTypes", userService.getDocumentTypes());
            model.addAttribute("idoneidadList", idoneidadService.findMostRecentByIntermediary(id));

            // Obtener experiencias laborales y asegurar que nunca sea null
            List<ExperienciaLaboralDTO> experiencias = infraestructuraHumanaService
                    .findWorkExperienceByIntermediary(id);
            if (experiencias == null) {
                experiencias = new ArrayList<>();
            }
            model.addAttribute("experienciasLaborales", experiencias);

            model.addAttribute("departments", ubicacionService.getAllDepartments());
            model.addAttribute("cities", ubicacionService.getAllCities());

            // Cargar estados de campos para observaciones
            Map<String, FormFieldStateDTO> fieldStates = intermediaryService.getFieldStates(id);
            model.addAttribute("fieldStates", fieldStates);

            // Cargar infraestructuras y firma
            loadInfrastructuraOperativa(id, model);
            loadFirmaDigitalizada(id, model);

            log.info(
                    "Modelo preparado para renderizar: intermediary={}, idoneidadList.size={}, experienciasLaborales.size={}",
                    intermediary.getId(),
                    model.getAttribute("idoneidadList") != null ? ((List<?>) model.getAttribute("idoneidadList")).size()
                            : 0,
                    experiencias.size());

            return "intermediary/edit-intermediary";

        } catch (Exception e) {
            log.error("Error al preparar el formulario de complemento para ID {}: {}", id, e.getMessage(), e);
            return "redirect:/error";
        }
    }

    @GetMapping("/field-observation/{id}/{field}")
    @ResponseBody
    public ResponseEntity<?> getFieldObservation(@PathVariable Long id, @PathVariable String field) {
        try {
            // En lugar de buscar por columna, utilizamos el endpoint existente
            return ResponseEntity.ok(intermediaryService.getFieldState(id, field));
        } catch (Exception e) {
            log.error("Error al obtener observación de campo: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/fields-with-observations/{id}")
    @ResponseBody
    public ResponseEntity<?> getFieldsWithObservations(@PathVariable Long id) {
        try {
            // Buscar todas las observaciones para este intermediario
            List<RuiHistoryDetails> details = historyDetailsRepository.findByIntermediaryId(id);

            // Extraer los nombres de los campos que tienen observaciones
            List<String> fieldsWithObservations = details.stream()
                    .map(RuiHistoryDetails::getFieldName) // Corregido a fieldName
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("fields", fieldsWithObservations));
        } catch (Exception e) {
            log.error("Error al obtener campos con observaciones: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/idoneidad-has-observation/{id}")
    @ResponseBody
    public ResponseEntity<?> checkIdoneidadHasObservation(@PathVariable Long id) {
        try {
            // Asumo que las observaciones de idoneidad se almacenan con tableName =
            // "RUI_IDONIEDAD"
            List<RuiHistoryDetails> observations = historyDetailsRepository
                    .findByTableIdAndTableName(id, "RUI_IDONIEDAD");

            boolean hasObservation = !observations.isEmpty();
            return ResponseEntity.ok(Map.of("hasObservation", hasObservation));
        } catch (Exception e) {
            log.error("Error al verificar observación de idoneidad: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("hasObservation", false));
        }
    }

    @GetMapping("/workexp-has-observation/{id}")
    @ResponseBody
    public ResponseEntity<?> checkWorkExpHasObservation(@PathVariable Long id) {
        try {
            // Asumo que las observaciones de experiencia laboral se almacenan con tableName
            // = "RUI_WORK_EXPERIENCE"
            List<RuiHistoryDetails> observations = historyDetailsRepository
                    .findByTableIdAndTableName(id, "RUI_WORK_EXPERIENCE");

            boolean hasObservation = !observations.isEmpty();
            return ResponseEntity.ok(Map.of("hasObservation", hasObservation));
        } catch (Exception e) {
            log.error("Error al verificar observación de experiencia laboral: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("hasObservation", false));
        }
    }

    // Método GET para mostrar el formulario simplificado
    @GetMapping("/test/{id}")
    public String showTestForm(@PathVariable Long id, Model model) {
        log.debug("Mostrando formulario simplificado para ID: {}", id);
        model.addAttribute("intermediaryId", id); // Pasar el ID al formulario
        return "intermediary/test"; // Nombre del template (test.html)
    }

    // Método POST para procesar el formulario simplificado
    @PostMapping("/test/{id}")
    public String complementIntermediarySimplified(
            @PathVariable Long id,
            @RequestParam(name = "idoneidadFile", required = false) MultipartFile idoneidadFile,
            @RequestParam(name = "workExperienceFile", required = false) MultipartFile workExperienceFile,
            MultipartHttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            log.debug("Inicio de complementIntermediarySimplified para ID: {}", id);

            // Autenticación
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.error("No hay usuario autenticado");
                return "redirect:/auth/login";
            }
            String username = authentication.getName();
            log.debug("Usuario autenticado: {}", username);
            Long currentUserId = getCurrentUserId();

            // Depuración
            log.debug("Request Content-Type: {}", request.getContentType());
            log.debug("Todos los archivos recibidos:");
            request.getFileMap().forEach((name, file) -> log.debug("Archivo: name={}, originalFilename={}, size={}",
                    name, file.getOriginalFilename(), file.getSize()));
            log.debug("idoneidadFile: {}", idoneidadFile != null ? idoneidadFile.getOriginalFilename() : "null");
            log.debug("workExperienceFile: {}",
                    workExperienceFile != null ? workExperienceFile.getOriginalFilename() : "null");

            // Procesar archivos
            if (idoneidadFile != null && !idoneidadFile.isEmpty()) {
                String filePath = fileStorageService.storeFile(idoneidadFile, "IDONIEDAD/test", currentUserId, id);
                log.info("Archivo de idoneidad guardado en: {}", filePath);
            }
            if (workExperienceFile != null && !workExperienceFile.isEmpty()) {
                String filePath = fileStorageService.storeFile(workExperienceFile, "WORK_EXPERIENCE/test",
                        currentUserId, id);
                log.info("Archivo de experiencia laboral guardado en: {}", filePath);
            }

            redirectAttributes.addFlashAttribute("mensaje", "Formulario simplificado procesado correctamente");
            return "redirect:/intermediary/my-registries";
        } catch (Exception e) {
            log.error("Error al procesar el formulario simplificado: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al procesar el formulario: " + e.getMessage());
            return "redirect:/intermediary/my-registries";
        }
    }

    // Endpoints para crear un nuevo intermediario
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        // Preparar un intermediario vacío
        RuiIntermediary intermediary = new RuiIntermediary();

        // Inicializar company y person para evitar NullPointerException
        intermediary.setCompanyId(new RuiCompany());
        intermediary.setPersonId(new RuiPerson());

        model.addAttribute("intermediary", intermediary);
        model.addAttribute("departments", ubicacionService.getAllDepartments());
        model.addAttribute("cities", ubicacionService.getAllCities());
        model.addAttribute("documentTypes", documentTypeService.getAllDocumentTypes());
        model.addAttribute("intermediaryTypes", intermediaryTypeService.getAllIntermediaryTypes());
        model.addAttribute("activeTab", "create");

        return "intermediary/create-intermediary";
    }

    @PostMapping("/create")
    public String createIntermediary(
            @ModelAttribute RuiIntermediary intermediary,
            @RequestParam(value = "typeIntermediary", required = true) Long intermediaryTypeId,
            @RequestParam(value = "isCompany", required = false) Boolean isCompany,
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "cityId", required = false) Long cityId,
            @RequestParam(value = "personDepartmentId", required = false) Long personDepartmentId,
            @RequestParam(value = "personCityId", required = false) Long personCityId,
            @RequestParam(value = "documentType", required = false) String documentType,
            @RequestParam(required = false) MultipartFile idoneityFile,
            @RequestParam(required = false) MultipartFile ccFile,
            @RequestParam(required = false) MultipartFile softwareFile,
            @RequestParam(required = false) MultipartFile equipmentFile,
            @RequestParam(required = false) MultipartFile signatureFile,
            MultipartHttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            log.info("Iniciando creación de intermediario");

            // Obtener usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.error("No hay usuario autenticado");
                redirectAttributes.addFlashAttribute("error", "Debe iniciar sesión para crear un intermediario");
                return "redirect:/auth/login";
            }

            String username = authentication.getName();
            log.info("Usuario autenticado: {}", username);

            Optional<RuiUser> userOpt = ruiUserRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/";
            }

            RuiUser user = userOpt.get();

            // Establecer tipo de intermediario
            RuiGenerics tipoIntermediario = new RuiGenerics();
            tipoIntermediario.setId(intermediaryTypeId);
            intermediary.setTypeIntermediarieId(tipoIntermediario);

            // Determinar si es persona o empresa basado en el tipo de intermediario
            boolean isAgente = (intermediaryTypeId == 4L);

            if (isAgente) {
                // Es un agente (persona)
                intermediary.setCompanyId(null);

                // Configurar datos de persona
                RuiPerson person = intermediary.getPersonId();
                person.setDocumentType(documentType);
                person.setStatus(1); // Activo

                // Configurar ubicación
                if (personDepartmentId != null && personCityId != null) {
                    // Obtenemos directamente las entidades del repositorio
                    Optional<RuiDepartment> department = ruiDepartmentRepository.findById(personDepartmentId);
                    Optional<RuiCity> city = ruiCityRepository.findById(personCityId);

                    // Asignamos las entidades
                    department.ifPresent(person::setDepartmentId);
                    city.ifPresent(person::setCityId);
                }
            } else {
                // Es una empresa
                intermediary.setPersonId(null);

                RuiCompany company = intermediary.getCompanyId();
                // No hay campo status en RuiCompany, así que eliminamos la línea
                // company.setStatus(1);

                // Configurar ubicación
                if (departmentId != null && cityId != null) {
                    // Obtenemos directamente las entidades del repositorio
                    Optional<RuiDepartment> department = ruiDepartmentRepository.findById(departmentId);
                    Optional<RuiCity> city = ruiCityRepository.findById(cityId);

                    // Asignamos las entidades
                    department.ifPresent(company::setDepartmentId);
                    city.ifPresent(company::setCityId);
                }
            }

            // Establecer estado inicial y número de radicado
            intermediary.setState(IntermediaryState.OPENING);
            intermediary.setRadicateNumber(generateRadicateNumber());

            // Guardar el intermediario
            RuiIntermediary savedIntermediary = intermediaryService.create(intermediary, user);

            // Manejo de archivos adjuntos
            Long intermediaryId = savedIntermediary.getId();
            Long userId = user.getId();

            // Procesar archivos si fueron proporcionados
            processFiles(idoneityFile, ccFile, softwareFile, equipmentFile, signatureFile, intermediaryId, userId);

            redirectAttributes.addFlashAttribute("mensaje", "Intermediario creado correctamente");
            return "redirect:/intermediary/my-registries";

        } catch (Exception e) {
            log.error("Error al crear el intermediario: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al crear el intermediario: " + e.getMessage());
            return "redirect:/intermediary/create";
        }
    }

    private String generateRadicateNumber() {
        // Generar un número de radicado único basado en la fecha actual
        String prefix = "RUI-";
        String timestamp = String.valueOf(new Date().getTime()).substring(0, 10);
        return prefix + timestamp;
    }

    private void processFiles(
            MultipartFile idoneityFile,
            MultipartFile ccFile,
            MultipartFile softwareFile,
            MultipartFile equipmentFile,
            MultipartFile signatureFile,
            Long intermediaryId,
            Long userId) {

        try {
            if (idoneityFile != null && !idoneityFile.isEmpty()) {
                String filePath = fileStorageService.storeFile(idoneityFile, "IDONIEDAD", userId, intermediaryId);
                log.info("Archivo de idoneidad guardado: {}", filePath);
                // Aquí podrías guardar la referencia al archivo en la base de datos
            }

            if (ccFile != null && !ccFile.isEmpty()) {
                String filePath = fileStorageService.storeFile(ccFile, "INFRA_OPERACIONAL/CC", userId, intermediaryId);
                log.info("Archivo de cámara de comercio guardado: {}", filePath);
            }

            if (softwareFile != null && !softwareFile.isEmpty()) {
                String filePath = fileStorageService.storeFile(softwareFile, "INFRA_OPERACIONAL/SOFT", userId,
                        intermediaryId);
                log.info("Archivo de software guardado: {}", filePath);
            }

            if (equipmentFile != null && !equipmentFile.isEmpty()) {
                String filePath = fileStorageService.storeFile(equipmentFile, "INFRA_OPERACIONAL/HARD", userId,
                        intermediaryId);
                log.info("Archivo de equipos guardado: {}", filePath);
            }

            if (signatureFile != null && !signatureFile.isEmpty()) {
                String filePath = fileStorageService.storeFile(signatureFile, "FIRMA_DIGITALIZADA", userId,
                        intermediaryId);
                log.info("Archivo de firma digitalizada guardado: {}", filePath);
            }
        } catch (Exception e) {
            log.error("Error al procesar archivos: {}", e.getMessage(), e);
        }
    }
}