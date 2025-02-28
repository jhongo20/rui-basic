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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rui.basic.app.basic.service.DocumentoService;
import com.rui.basic.app.basic.service.FileStorageService;
import com.rui.basic.app.basic.service.FirmaDigitalizadaService;
import com.rui.basic.app.basic.service.IdoneidadProfesionalService;
import com.rui.basic.app.basic.service.InfraestructuraHumanaService;
import com.rui.basic.app.basic.service.InfraestructuraOperativaService;
import com.rui.basic.app.basic.service.IntermediaryService;
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
import lombok.Data;

import com.rui.basic.app.basic.domain.entities.RuiCity;
import com.rui.basic.app.basic.domain.entities.RuiCompany;
import com.rui.basic.app.basic.domain.entities.RuiDepartment;
import com.rui.basic.app.basic.domain.entities.RuiHistoryDetails;
import com.rui.basic.app.basic.domain.entities.RuiIdoniedad;
import com.rui.basic.app.basic.domain.entities.RuiInfraHuman;
import com.rui.basic.app.basic.domain.entities.RuiInfraOperational;
import com.rui.basic.app.basic.domain.entities.RuiIntermediary;
import com.rui.basic.app.basic.domain.entities.RuiPerson;
import com.rui.basic.app.basic.domain.entities.RuiUser;
import com.rui.basic.app.basic.domain.entities.RuiWorkExperience;
import com.rui.basic.app.basic.domain.enums.IntermediaryState;
import com.rui.basic.app.basic.repository.RuiCityRepository;
import com.rui.basic.app.basic.repository.RuiDepartmentRepository;
import com.rui.basic.app.basic.repository.RuiHistoryDetailsRepository;
import com.rui.basic.app.basic.repository.RuiUserRepository;

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
            RuiDepartmentRepository ruiDepartmentRepository) {
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
            // Obtener el usuario actual (deberías implementar esto según tu sistema de autenticación)
            Long currentUserId = getCurrentUserId();
            
            // Almacenar el archivo
            String filePath = fileStorageService.storeFile(
                file,
                String.format("intermediary/%d/%s", intermediaryId, type),
                currentUserId,
                intermediaryId
            );
            
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
            InfrastructuraHumanaDTO infraestructuraHumana = 
                infraestructuraHumanaService.findByIntermediary(id);
            model.addAttribute("infraestructuraHumana", infraestructuraHumana);

            /*
              List<ExperienciaLaboralDTO> experiencias = 
                infraestructuraHumanaService.findWorkExperienceByIntermediary(id);
            model.addAttribute("experienciasLaborales", experiencias);
             */
            if (infraestructuraHumana != null && infraestructuraHumana.getId() != null) {
                List<ExperienciaLaboralDTO> experiencias = 
                    infraestructuraHumanaService.findWorkExperienceByInfraHuman(
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
                List<ExperienciaLaboralDTO> experiencias = (List<ExperienciaLaboralDTO>) model.getAttribute("experienciasLaborales");
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


    //endpoint para documentos de tab infraestructura operativa
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
                responseBuilder.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"");
            } else {
                // Configurar para visualización en línea
                responseBuilder.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"");
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

    //endpoint para observaciones de tab infraestructura operativa
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

    


    //--------------------------------------------------------------------------------
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

            Page<RuiIntermediary> userRegistries = intermediaryService.findByUser(username, page, size, sort, direction);

            model.addAttribute("registries", userRegistries.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", userRegistries.getTotalPages());
            model.addAttribute("totalItems", userRegistries.getTotalElements());
            model.addAttribute("sort", sort);
            model.addAttribute("direction", direction);

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
   /*  private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // Asumimos que el ID del usuario está en los detalles del Authentication o como principal
            // Debes ajustarlo según tu implementación de autenticación
            return 1L; // Temporal, reemplaza con la lógica real
        }
        throw new IllegalStateException("No hay usuario autenticado");
    }*/

    //--------------------------------------------------------------------------------
    // métodos para editar 
    @PostMapping("/complement/{id}")
public String complementIntermediary(@PathVariable Long id,
                                    @ModelAttribute RuiIntermediary intermediary,
                                    // Parámetros para los archivos
                                    @RequestParam(required = false) MultipartFile ccFile,
                                    @RequestParam(required = false) MultipartFile softFile,
                                    @RequestParam(required = false) MultipartFile hardFile,
                                    @RequestParam(required = false) MultipartFile signatureFile,
                                    @RequestParam(name = "idoneidadFiles", required = false) List<MultipartFile> idoneidadFiles,
                                    // Parámetros para experiencias laborales
                                    @RequestParam(name = "workExperienceIds", required = false) List<Long> workExpIds,
                                    @RequestParam(name = "workExperienceCompanies", required = false) List<String> workExpCompanies,
                                    @RequestParam(name = "workExperienceCharges", required = false) List<String> workExpCharges,
                                    @RequestParam(name = "workExperienceNameBosses", required = false) List<String> workExpNameBosses,
                                    @RequestParam(name = "workExperiencePhoneBosses", required = false) List<String> workExpPhoneBosses,
                                    @RequestParam(name = "workExperienceStartDates", required = false) List<String> workExpStartDates,
                                    @RequestParam(name = "workExperienceEndDates", required = false) List<String> workExpEndDates,
                                    @RequestParam(name = "workExperienceFiles", required = false) List<MultipartFile> workExperienceFiles,
                                    // Otros parámetros
                                    @RequestParam Map<String, String> allParams,
                                    RedirectAttributes redirectAttributes) {
    try {
        // Obtener el usuario actual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("No hay usuario autenticado");
            return "redirect:/auth/login";
        }
        
        String username = authentication.getName();
        Long currentUserId = getCurrentUserId();
        
        // Obtener el intermediario existente con todos sus detalles
        RuiIntermediary existingIntermediary = intermediaryService.findByIdWithDetails(id);
        
        // Verificar que el intermediario pertenece al usuario actual
        boolean authorized = isUserAuthorizedForIntermediary(existingIntermediary, username);
        if (!authorized) {
            log.warn("Usuario {} no autorizado para editar intermediario {}", username, id);
            redirectAttributes.addFlashAttribute("error", "No está autorizado para editar este registro");
            return "redirect:/intermediary/my-registries";
        }

        // Actualizar datos básicos
        updateIntermediaryDetails(existingIntermediary, intermediary);

        // Actualizar idoneidad desde los parámetros del formulario
        List<RuiIdoniedad> existingIdoneidad = existingIntermediary.getRuiIdoniedadList();
        if (existingIdoneidad != null) {
            for (int i = 0; i < existingIdoneidad.size(); i++) {
                String courseEntityKey = "idoneidadList[" + i + "].courseEntity";
                String dateCourseKey = "idoneidadList[" + i + "].dateCourse";
                if (allParams.containsKey(courseEntityKey)) {
                    existingIdoneidad.get(i).setCourseEntity(allParams.get(courseEntityKey));
                }
                if (allParams.containsKey(dateCourseKey)) {
                    String dateStr = allParams.get(dateCourseKey);
                    if (dateStr != null && !dateStr.isEmpty()) {
                        try {
                            existingIdoneidad.get(i).setDateCourse(new SimpleDateFormat("yyyy-MM-dd").parse(dateStr));
                        } catch (Exception e) {
                            log.warn("Error al parsear fecha {}: {}", dateStr, e.getMessage());
                        }
                    }
                }
            }
        }
        
        // Actualizar experiencias laborales
        if (workExpIds != null && !workExpIds.isEmpty() && 
            existingIntermediary.getInfrastructureHumanId() != null) {
            
            Set<RuiWorkExperience> experiences = existingIntermediary.getInfrastructureHumanId().getWorkExperiences();
            if (experiences != null) {
                for (int i = 0; i < workExpIds.size(); i++) {
                    Long expId = workExpIds.get(i);
                    
                    for (RuiWorkExperience exp : experiences) {
                        if (exp.getId().equals(expId)) {
                            // Actualizar datos
                            if (i < workExpCompanies.size()) exp.setCompany(workExpCompanies.get(i));
                            if (i < workExpCharges.size()) exp.setCharge(workExpCharges.get(i));
                            if (i < workExpNameBosses.size()) exp.setNameBoss(workExpNameBosses.get(i));
                            if (i < workExpPhoneBosses.size()) exp.setPhoneBoss(workExpPhoneBosses.get(i));
                            
                            // Convertir fechas si están presentes
                            if (i < workExpStartDates.size() && workExpStartDates.get(i) != null && !workExpStartDates.get(i).isEmpty()) {
                                try {
                                    exp.setStartDate(new SimpleDateFormat("yyyy-MM-dd").parse(workExpStartDates.get(i)));
                                } catch (Exception e) {
                                    log.warn("Error parsing start date: {}", e.getMessage());
                                }
                            }
                            
                            if (i < workExpEndDates.size() && workExpEndDates.get(i) != null && !workExpEndDates.get(i).isEmpty()) {
                                try {
                                    exp.setEndDate(new SimpleDateFormat("yyyy-MM-dd").parse(workExpEndDates.get(i)));
                                } catch (Exception e) {
                                    log.warn("Error parsing end date: {}", e.getMessage());
                                }
                            }
                            
                            break;
                        }
                    }
                }
            }
        }

        // Cambiar estado a COMPLEMENTED
        intermediaryService.updateIntermediaryStatus(id, IntermediaryState.COMPLEMENTED, "Información complementada", null);

        // Cargar archivos de Infraestructura Operativa
        if (ccFile != null && !ccFile.isEmpty()) {
            String filePath = fileStorageService.storeFile(ccFile, "INFRA_OPERACIONAL/CC", currentUserId, id);
            intermediaryService.saveSupport(id, filePath, "INFRA_OPERACIONAL");
        }
        if (softFile != null && !softFile.isEmpty()) {
            String filePath = fileStorageService.storeFile(softFile, "INFRA_OPERACIONAL/SOFT", currentUserId, id);
            intermediaryService.saveSupport(id, filePath, "INFRA_OPERACIONAL");
        }
        if (hardFile != null && !hardFile.isEmpty()) {
            String filePath = fileStorageService.storeFile(hardFile, "INFRA_OPERACIONAL/HARD", currentUserId, id);
            intermediaryService.saveSupport(id, filePath, "INFRA_OPERACIONAL");
        }

        // Cargar archivos de Idoneidad
        if (idoneidadFiles != null && existingIdoneidad != null) {
            for (int i = 0; i < idoneidadFiles.size() && i < existingIdoneidad.size(); i++) {
                MultipartFile file = idoneidadFiles.get(i);
                if (file != null && !file.isEmpty()) {
                    Long idoneidadId = existingIdoneidad.get(i).getId();
                    String filePath = fileStorageService.storeFile(file, "IDONIEDAD/" + idoneidadId, currentUserId, id);
                    intermediaryService.saveSupport(id, filePath, "IDONIEDAD");
                }
            }
        }

        // Cargar archivos de Experiencia Laboral
        if (workExperienceFiles != null && workExpIds != null && 
            existingIntermediary.getInfrastructureHumanId() != null) {
            
            for (int i = 0; i < workExperienceFiles.size() && i < workExpIds.size(); i++) {
                MultipartFile file = workExperienceFiles.get(i);
                if (file != null && !file.isEmpty()) {
                    Long workExpId = workExpIds.get(i);
                    String filePath = fileStorageService.storeFile(file, "WORK_EXPERIENCE/" + workExpId, currentUserId, id);
                    intermediaryService.saveSupport(id, filePath, "WORK_EXPERIENCE");
                }
            }
        }

        // Cargar firma digitalizada
        if (signatureFile != null && !signatureFile.isEmpty()) {
            String filePath = fileStorageService.storeFile(signatureFile, "FIRMA_DIGITALIZADA", currentUserId, id);
            intermediaryService.saveSupport(id, filePath, "FIRMA_DIGITALIZADA");
        }

        redirectAttributes.addFlashAttribute("mensaje", "Registro complementado correctamente");
        return "redirect:/intermediary/my-registries";
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
        if (updated.getCompanyId().getDepartmentId() != null && updated.getCompanyId().getDepartmentId().getId() != null) {
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
        if (updated.getPersonId().getDepartmentId() != null && updated.getPersonId().getDepartmentId().getId() != null) {
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
                        if (existingExpItem.getId() != null && existingExpItem.getId().equals(updatedExpItem.getId())) {
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
 * Método auxiliar para verificar si el usuario está autorizado para el intermediario
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
                 intermediary.getTypeIntermediarieId() != null ? intermediary.getTypeIntermediarieId().getValue() : "null");

        // Determinar si es agente por el tipo de intermediario
        boolean isAgente = intermediary.getTypeIntermediarieId() != null && intermediary.getTypeIntermediarieId().getId() == 4L;

        // Inicializar objetos del tab de información general para evitar NullPointerException
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
        List<ExperienciaLaboralDTO> experiencias = infraestructuraHumanaService.findWorkExperienceByIntermediary(id);
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

        log.info("Modelo preparado para renderizar: intermediary={}, idoneidadList.size={}, experienciasLaborales.size={}", 
                 intermediary.getId(), 
                 model.getAttribute("idoneidadList") != null ? ((List<?>) model.getAttribute("idoneidadList")).size() : 0,
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
                .map(RuiHistoryDetails::getFieldName)  // Corregido a fieldName
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
            // Asumo que las observaciones de idoneidad se almacenan con tableName = "RUI_IDONIEDAD"
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
            // Asumo que las observaciones de experiencia laboral se almacenan con tableName = "RUI_WORK_EXPERIENCE"
            List<RuiHistoryDetails> observations = historyDetailsRepository
                .findByTableIdAndTableName(id, "RUI_WORK_EXPERIENCE");
            
            boolean hasObservation = !observations.isEmpty();
            return ResponseEntity.ok(Map.of("hasObservation", hasObservation));
        } catch (Exception e) {
            log.error("Error al verificar observación de experiencia laboral: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("hasObservation", false));
        }
    }

}