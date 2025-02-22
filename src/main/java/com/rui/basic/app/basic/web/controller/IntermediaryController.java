package com.rui.basic.app.basic.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

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
import com.rui.basic.app.basic.service.email.EmailService;
import com.rui.basic.app.basic.web.dto.EmailTemplateDTO;
import com.rui.basic.app.basic.web.dto.ExperienciaLaboralDTO;
import com.rui.basic.app.basic.web.dto.FirmaDigitalizadaDTO;
import com.rui.basic.app.basic.web.dto.IdoneidadProfesionalDTO;
import com.rui.basic.app.basic.web.dto.InfraestructuraOperativaDTO;
import com.rui.basic.app.basic.web.dto.InfrastructuraHumanaDTO;
import com.rui.basic.app.basic.domain.entities.RuiIntermediary;
import com.rui.basic.app.basic.domain.enums.IntermediaryState;

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
    

    
    
    public IntermediaryController(
            IntermediaryService intermediaryService,
            EmailService emailService,
            FileStorageService fileStorageService,
            IdoneidadProfesionalService idoneidadService,
            InfraestructuraHumanaService infraestructuraHumanaService,
            DocumentoService documentoService,
            InfraestructuraOperativaService infraestructuraOperativaService,
            FirmaDigitalizadaService firmaDigitalizadaService,
            UbicacionService ubicacionService) {
        this.intermediaryService = intermediaryService;
        this.emailService = emailService;
        this.fileStorageService = fileStorageService;
        this.idoneidadService = idoneidadService;
        this.infraestructuraHumanaService = infraestructuraHumanaService;
        this.documentoService = documentoService;
        this.infraestructuraOperativaService = infraestructuraOperativaService;
        this.firmaDigitalizadaService = firmaDigitalizadaService;
        this.ubicacionService = ubicacionService;
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
            // Obtener el intermediario
            RuiIntermediary intermediary = intermediaryService.findById(id);
            
            if (intermediary == null) {
                return "redirect:/error";
            }
            
            // Obtener la idoneidad profesional más reciente
            List<IdoneidadProfesionalDTO> idoneidadList = idoneidadService.findMostRecentByIntermediary(id);
            
            // Modelo por defecto
            model.addAttribute("intermediary", intermediary);
            model.addAttribute("idoneidadList", idoneidadList);

            // Obtener datos de departamento y ciudad si es una empresa
        if (intermediary.getCompanyId() != null) {
            Long departmentId = intermediary.getCompanyId().getDepartmentId() != null ? 
                                intermediary.getCompanyId().getDepartmentId().getId() : null;
            Long cityId = intermediary.getCompanyId().getCityId() != null ? 
                          intermediary.getCompanyId().getCityId().getId() : null;
            
            if (departmentId != null) {
                ubicacionService.getDepartmentById(departmentId).ifPresent(dept -> 
                    model.addAttribute("departamento", dept));
            }
            
            if (cityId != null) {
                ubicacionService.getCityById(cityId).ifPresent(city -> 
                    model.addAttribute("ciudad", city));
            }
        } else if (intermediary.getPersonId() != null) {
            // Para personas
            Long departmentId = intermediary.getPersonId().getDepartmentId() != null ? 
                                intermediary.getPersonId().getDepartmentId().getId() : null;
            Long cityId = intermediary.getPersonId().getCityId() != null ? 
                          intermediary.getPersonId().getCityId().getId() : null;
            
            if (departmentId != null) {
                ubicacionService.getDepartmentById(departmentId).ifPresent(dept -> 
                    model.addAttribute("departamento", dept));
            }
            
            if (cityId != null) {
                ubicacionService.getCityById(cityId).ifPresent(city -> 
                    model.addAttribute("ciudad", city));
            }
        }
            
            try {
                // Intentar obtener información de infraestructura humana
                InfrastructuraHumanaDTO infraestructuraHumana = infraestructuraHumanaService.findByIntermediary(id);
                model.addAttribute("infraestructuraHumana", infraestructuraHumana);
                
                if (infraestructuraHumana != null && infraestructuraHumana.getId() != null) {
                    try {
                        // Intentar obtener experiencias laborales
                        List<ExperienciaLaboralDTO> experienciasLaborales = 
                            infraestructuraHumanaService.findWorkExperienceByInfraHuman(infraestructuraHumana.getId());
                        model.addAttribute("experienciasLaborales", experienciasLaborales);
                    } catch (Exception e) {
                        log.warn("Error al cargar experiencias laborales: {}", e.getMessage());
                        model.addAttribute("experienciasLaborales", new ArrayList<>());
                    }
                } else {
                    model.addAttribute("experienciasLaborales", new ArrayList<>());
                }
            } catch (Exception e) {
                log.warn("Error al cargar infraestructura humana: {}", e.getMessage());
                model.addAttribute("infraestructuraHumana", null);
                model.addAttribute("experienciasLaborales", new ArrayList<>());
            }
            
            try {
                // Intentar obtener información de infraestructura operativa
                InfraestructuraOperativaDTO infraestructuraOperativa = infraestructuraOperativaService.findByIntermediary(id);
                model.addAttribute("infraestructuraOperativa", infraestructuraOperativa);
            } catch (Exception e) {
                log.warn("Error al cargar infraestructura operativa: {}", e.getMessage());
                model.addAttribute("infraestructuraOperativa", null);
            }

            try {
                // Intentar obtener información de firma digitalizada
                FirmaDigitalizadaDTO firmaDigitalizada = firmaDigitalizadaService.findByIntermediary(id);
                model.addAttribute("firmaDigitalizada", firmaDigitalizada);
            } catch (Exception e) {
                log.warn("Error al cargar firma digitalizada: {}", e.getMessage());
                model.addAttribute("firmaDigitalizada", null);
            }
            
            // Añadir estado actual para mostrar botones apropiados
            model.addAttribute("estadoRevisado", intermediary.getState() == IntermediaryState.REVIEWED);
            
            return "intermediary/review";
        } catch (Exception e) {
            log.error("Error al cargar la revisión del intermediario: {}", e.getMessage(), e);
            return "redirect:/error";
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

    

    @GetMapping("/view")
    public String viewIntermediary() {
        return "intermediary/view";
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
    public String marcarComoRevisado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean success = firmaDigitalizadaService.marcarComoRevisado(id);
        
        if (success) {
            redirectAttributes.addFlashAttribute("mensaje", "Registro marcado como revisado correctamente");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error al marcar como revisado");
        }
        
        return "redirect:/intermediary/review/" + id;
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

    
    // Método auxiliar para obtener el ID del usuario actual
    private Long getCurrentUserId() {
        // Implementa esto según tu sistema de autenticación
        // Por ejemplo, usando Spring Security
        return 1L; // Valor temporal
    }
}