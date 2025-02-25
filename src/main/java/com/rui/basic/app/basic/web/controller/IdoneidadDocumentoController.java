package com.rui.basic.app.basic.web.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rui.basic.app.basic.service.DocumentoService;
import com.rui.basic.app.basic.service.IdoneidadProfesionalService;
import com.rui.basic.app.basic.web.dto.FormFieldStateDTO;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/api/idoniedad")
public class IdoneidadDocumentoController {

    private static final Logger log = LoggerFactory.getLogger(IdoneidadDocumentoController.class);
    private final DocumentoService documentoService;
    private final IdoneidadProfesionalService idoneidadService;
    public IdoneidadDocumentoController(DocumentoService documentoService, IdoneidadProfesionalService idoneidadService) {
        this.documentoService = documentoService;
        this.idoneidadService = idoneidadService;
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        try {
            Resource file = documentoService.loadIdoneidadDocument(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (Exception e) {
            log.error("Error al descargar documento de idoneidad: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewDocument(@PathVariable Long id) {
        try {
            log.info("Solicitando visualización de documento de idoneidad ID: {}", id);
            Resource file = documentoService.loadIdoneidadDocument(id);
            
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
            log.error("Error al visualizar documento de idoneidad: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    // Método auxiliar para determinar el tipo de contenido
    private String determineContentType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            default:
                return "application/octet-stream";
        }
    }

    @GetMapping("/{idoniedadId}/observation")
    @ResponseBody
    public ResponseEntity<FormFieldStateDTO> getObservation(
            @PathVariable Long idoniedadId) {
        try {
            log.debug("Recibiendo solicitud GET para Idoneidad ID: {}", idoniedadId);
            FormFieldStateDTO state = idoneidadService.getObservation(idoniedadId);
            log.info("Estado de observación obtenido para Idoneidad ID: {}", idoniedadId);
            return ResponseEntity.ok(state);
        } catch (EntityNotFoundException e) {
            log.error("Idoneidad no encontrada: {}", idoniedadId, e);
            FormFieldStateDTO state = new FormFieldStateDTO();
            state.setIconClose(false);
            state.setCommentDisabled(true);
            state.setObservation("Idoneidad no encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(state);
        } catch (Exception e) {
            log.error("Error al obtener estado del registro de idoneidad para ID {}: {}", idoniedadId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(new FormFieldStateDTO());
        }
    }
    
    @PostMapping("/{idoniedadId}/observation")
    @ResponseBody
    public ResponseEntity<FormFieldStateDTO> createObservation(
            @PathVariable Long idoniedadId) {
        try {
            log.debug("Recibiendo solicitud POST para crear observación en Idoneidad ID: {}", idoniedadId);
            FormFieldStateDTO state = idoneidadService.createObservation(idoniedadId);
            log.info("Observación creada exitosamente para Idoneidad ID: {}", idoniedadId);
            return ResponseEntity.ok(state);
        } catch (EntityNotFoundException e) {
            log.error("Idoneidad no encontrada: {}", idoniedadId, e);
            FormFieldStateDTO state = new FormFieldStateDTO();
            state.setIconClose(false);
            state.setCommentDisabled(true);
            state.setObservation("Idoneidad no encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(state);
        } catch (Exception e) {
            log.error("Error al crear observación para Idoneidad ID {}: {}", idoniedadId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(new FormFieldStateDTO());
        }
    }
    
    @DeleteMapping("/{idoniedadId}/observation")
    @ResponseBody
    public ResponseEntity<FormFieldStateDTO> removeObservation(
            @PathVariable Long idoniedadId) {
        try {
            log.debug("Recibiendo solicitud DELETE para eliminar observación en Idoneidad ID: {}", idoniedadId);
            FormFieldStateDTO state = idoneidadService.removeObservation(idoniedadId);
            log.info("Observación eliminada exitosamente para Idoneidad ID: {}", idoniedadId);
            return ResponseEntity.ok(state);
        } catch (EntityNotFoundException e) {
            log.error("Idoneidad no encontrada: {}", idoniedadId, e);
            FormFieldStateDTO state = new FormFieldStateDTO();
            state.setIconClose(false);
            state.setCommentDisabled(true);
            state.setObservation("Idoneidad no encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(state);
        } catch (Exception e) {
            log.error("Error al eliminar observación para Idoneidad ID {}: {}", idoniedadId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(new FormFieldStateDTO());
        }
    }
    
    @PutMapping("/{idoniedadId}/observation")
    @ResponseBody
    public ResponseEntity<FormFieldStateDTO> updateObservation(
            @PathVariable Long idoniedadId,
            @RequestBody Map<String, String> payload) {
        try {
            String observation = payload != null ? payload.get("observation") : null;
            if (observation == null || observation.trim().isEmpty()) {
                throw new IllegalArgumentException("La observación no puede ser nula o vacía");
            }
            log.debug("Recibiendo solicitud PUT para actualizar observación en Idoneidad ID: {}", idoniedadId);
            FormFieldStateDTO state = idoneidadService.updateObservation(idoniedadId, observation);
            log.info("Observación actualizada exitosamente para Idoneidad ID: {}", idoniedadId);
            return ResponseEntity.ok(state);
        } catch (IllegalArgumentException e) {
            log.error("Observación inválida para Idoneidad ID {}: {}", idoniedadId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(new FormFieldStateDTO());
        } catch (EntityNotFoundException e) {
            log.error("Idoneidad no encontrada: {}", idoniedadId, e);
            FormFieldStateDTO state = new FormFieldStateDTO();
            state.setIconClose(false);
            state.setCommentDisabled(true);
            state.setObservation("Idoneidad no encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(state);
        } catch (Exception e) {
            log.error("Error al actualizar observación para Idoneidad ID {}: {}", idoniedadId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(new FormFieldStateDTO());
        }
    }
}
