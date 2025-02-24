package com.rui.basic.app.basic.web.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rui.basic.app.basic.service.IntermediaryService;
import com.rui.basic.app.basic.web.dto.FormFieldStateDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/intermediary")
public class ObservationApiController {
    
    private final IntermediaryService intermediaryService;
    
    public ObservationApiController(IntermediaryService intermediaryService) {
        this.intermediaryService = intermediaryService;
    }
    
    @GetMapping("/{intermediaryId}/observation/{field}")
public ResponseEntity<FormFieldStateDTO> getObservation(
        @PathVariable Long intermediaryId,
        @PathVariable String field) {
    try {
        FormFieldStateDTO state = intermediaryService.getFieldState(intermediaryId, field);
        return ResponseEntity.ok(state);
    } catch (Exception e) {
        log.error("Error al obtener estado del campo: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(new FormFieldStateDTO());
    }
}
    
@PostMapping("/{intermediaryId}/observation/{field}")
public ResponseEntity<FormFieldStateDTO> createObservation(
        @PathVariable Long intermediaryId,
        @PathVariable String field) {
    try {
        intermediaryService.createObservation(intermediaryId, field);
        FormFieldStateDTO state = intermediaryService.getFieldState(intermediaryId, field);
        return ResponseEntity.ok(state);
    } catch (Exception e) {
        log.error("Error al crear observación: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(new FormFieldStateDTO());
    }
}
    
    @DeleteMapping("/{intermediaryId}/observation/{field}")
    public ResponseEntity<?> removeObservation(
            @PathVariable Long intermediaryId,
            @PathVariable String field) {
        try {
            intermediaryService.removeObservation(intermediaryId, field);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PutMapping("/{intermediaryId}/observation/{field}")
    public ResponseEntity<?> updateObservation(
            @PathVariable Long intermediaryId,
            @PathVariable String field,
            @RequestBody Map<String, String> payload) {
        try {
            String observation = payload.get("observation");
            intermediaryService.updateObservation(intermediaryId, field, observation);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
