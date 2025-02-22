package com.rui.basic.app.basic.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rui.basic.app.basic.service.DocumentoService;

@Controller
@RequestMapping("/api/idoniedad")
public class IdoneidadDocumentoController {

    private static final Logger log = LoggerFactory.getLogger(IdoneidadDocumentoController.class);
    private final DocumentoService documentoService;

    public IdoneidadDocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        try {
            log.info("Solicitando descarga de documento de idoneidad ID: {}", id);
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
            
            // Determinar el tipo de contenido basado en la extensión del archivo
            String contentType = determineContentType(file.getFilename());
            
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
}
