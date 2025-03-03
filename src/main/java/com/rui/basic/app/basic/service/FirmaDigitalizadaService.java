package com.rui.basic.app.basic.service;

import com.rui.basic.app.basic.domain.entities.RuiIntermediary;
import com.rui.basic.app.basic.domain.entities.RuiSupport;
import com.rui.basic.app.basic.domain.enums.IntermediaryState;
import com.rui.basic.app.basic.repository.RuiHistoryDetailsRepository;
import com.rui.basic.app.basic.repository.RuiIntermediaryRepository;
import com.rui.basic.app.basic.repository.RuiSupportRepository;
import com.rui.basic.app.basic.service.email.EmailService;
import com.rui.basic.app.basic.web.dto.EmailTemplateDTO;
import com.rui.basic.app.basic.web.dto.FirmaDigitalizadaDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FirmaDigitalizadaService {

    private static final Logger log = LoggerFactory.getLogger(FirmaDigitalizadaService.class);
    private final RuiSupportRepository supportRepository;
    private final RuiIntermediaryRepository intermediaryRepository;
    private final RuiHistoryDetailsRepository historyDetailsRepository;
    private final EmailService emailService;
    private final Path firmasStorageLocation;

   
    public FirmaDigitalizadaService(
            RuiSupportRepository supportRepository,
            RuiIntermediaryRepository intermediaryRepository,
            RuiHistoryDetailsRepository historyDetailsRepository,
            EmailService emailService,
            @Value("${app.documentos.ruta:/tmp/documentos}") String documentosRuta) {
        this.supportRepository = supportRepository;
        this.intermediaryRepository = intermediaryRepository;
        this.historyDetailsRepository = historyDetailsRepository;
        this.emailService = emailService;
        
        // Crear directorio para firmas
        this.firmasStorageLocation = Paths.get(documentosRuta, "firmas");
        try {
            Files.createDirectories(this.firmasStorageLocation);
        } catch (IOException e) {
            log.error("No se pudo inicializar el directorio de almacenamiento de firmas", e);
        }
    }

    public FirmaDigitalizadaDTO findByIntermediary(Long intermediaryId) {
        log.info("Buscando firma digitalizada para intermediario: {}", intermediaryId);
        
        try {
            Optional<RuiIntermediary> intermediaryOpt = intermediaryRepository.findById(intermediaryId);
            
            if (intermediaryOpt.isEmpty()) {
                log.info("No se encontró el intermediario: {}", intermediaryId);
                return null;
            }
            
            RuiIntermediary intermediary = intermediaryOpt.get();
            
            if (intermediary.getInfrastructureOperationalId() == null) {
                log.info("El intermediario no tiene infraestructura operativa asociada");
                return null;
            }
            
            //Optional<RuiSupport> supportOpt = supportRepository.findByInfraOperationalSign(intermediary.getInfrastructureOperationalId());
            Optional<RuiSupport> supportOpt = supportRepository.findByInfraOperationalSignAndStatus(
                    intermediary.getInfrastructureOperationalId(), (short) 1);

            if (supportOpt.isEmpty()) {
                log.info("No se encontró firma digitalizada para el intermediario: {}", intermediaryId);
                return null;
            }
            
            RuiSupport support = supportOpt.get();
            log.info("Firma digitalizada encontrada: ID={}, Filename={}", support.getId(), support.getFilename());
            return convertToDTO(supportOpt.get(), intermediaryId);  // Pasar el ID del intermediario
        } catch (Exception e) {
            log.error("Error al buscar firma digitalizada: {}", e.getMessage(), e);
            return null;
        }
    }
    
    public Resource loadFirmaDigitalizada(Long intermediaryId) {
        try {
            Optional<RuiIntermediary> intermediaryOpt = intermediaryRepository.findById(intermediaryId);
            
            if (intermediaryOpt.isEmpty()) {
                log.error("No se encontró el intermediario: {}", intermediaryId);
                throw new RuntimeException("Intermediario no encontrado");
            }
            
            RuiIntermediary intermediary = intermediaryOpt.get();
            
            if (intermediary.getInfrastructureOperationalId() == null) {
                log.error("El intermediario no tiene infraestructura operativa asociada");
                throw new RuntimeException("Infraestructura operativa no encontrada");
            }
            
            // Buscar el soporte de firma
            //Optional<RuiSupport> supportOpt = supportRepository.findByInfraOperationalSign(intermediary.getInfrastructureOperationalId());
            // Buscar el soporte activo
            Optional<RuiSupport> supportOpt = supportRepository.findByInfraOperationalSignAndStatus(
            intermediary.getInfrastructureOperationalId(), (short) 1);

            if (supportOpt.isEmpty()) {
                log.error("No se encontró firma digitalizada para el intermediario: {}", intermediaryId);
                throw new RuntimeException("Firma digitalizada no encontrada");
            }
            
            RuiSupport support = supportOpt.get();
            
            // Imprimir todos los detalles del soporte para diagnosticar
            log.info("Detalles del soporte de firma:");
            log.info("ID: {}", support.getId());
            log.info("Filename: {}", support.getFilename());
            log.info("Route: {}", support.getRoute());
            log.info("Extension: {}", support.getExtencion());
            
            // Construir la ruta completa del archivo
            String filePath = support.getRoute() + "/" + support.getFilename();
            log.info("Ruta completa del archivo: {}", filePath);
            
            // Comprobar si el archivo existe antes de intentar cargarlo
            File file = new File(filePath);
            if (!file.exists()) {
                log.error("El archivo no existe en la ruta: {}", filePath);
                throw new RuntimeException("El archivo no existe en la ruta especificada");
            }
            
            if (!file.canRead()) {
                log.error("El archivo no se puede leer en la ruta: {}", filePath);
                throw new RuntimeException("No se tiene permisos de lectura sobre el archivo");
            }
            
            log.info("El archivo existe: {} y es legible: {}", file.exists(), file.canRead());
            
            Path path = Paths.get(filePath);
            log.info("Path absoluto: {}", path.toAbsolutePath().toString());
            log.info("Path URI: {}", path.toUri().toString());
            
            Resource resource = new UrlResource(path.toUri());
            
            if (resource.exists() || resource.isReadable()) {
                log.info("Recurso cargado correctamente");
                return resource;
            } else {
                log.error("No se pudo leer el archivo aunque existe en el sistema de archivos");
                throw new RuntimeException("No se pudo leer el archivo");
            }
        } catch (Exception e) {
            log.error("Error al cargar firma digitalizada: {}", e.getMessage(), e);
            throw new RuntimeException("Error al cargar firma digitalizada", e);
        }
    }
    
    @Transactional
    public Map<String, Object> marcarComoRevisado(Long intermediaryId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        
        try {
            Optional<RuiIntermediary> intermediaryOpt = intermediaryRepository.findById(intermediaryId);
            
            if (intermediaryOpt.isEmpty()) {
                log.error("No se encontró el intermediario: {}", intermediaryId);
                result.put("message", "No se encontró el intermediario");
                return result;
            }
            
            RuiIntermediary intermediary = intermediaryOpt.get();
            
            // Verificar si hay observaciones pendientes
            Long observacionesPendientes = historyDetailsRepository.countByIntermediaryId(intermediaryId);
            
            // Si hay observaciones, indicarlo en la respuesta pero no impedir la acción
            if (observacionesPendientes > 0) {
                result.put("observacionesPendientes", true);
                result.put("cantidadObservaciones", observacionesPendientes);
            }
            
            // Cambiar estado
            intermediary.setState(IntermediaryState.REVIEWED);
            intermediaryRepository.save(intermediary);
            
            log.info("Intermediario {} marcado como REVISADO", intermediaryId);
            result.put("success", true);
            result.put("message", "Registro marcado como revisado correctamente");
            
            return result;
        } catch (Exception e) {
            log.error("Error al marcar como revisado el intermediario {}: {}", intermediaryId, e.getMessage(), e);
            result.put("message", "Error al marcar como revisado: " + e.getMessage());
            return result;
        }
    }
    
    @Transactional
    public boolean enviarAComplementar(Long intermediaryId) {
        try {
            Optional<RuiIntermediary> intermediaryOpt = intermediaryRepository.findById(intermediaryId);
            
            if (intermediaryOpt.isEmpty()) {
                log.error("No se encontró el intermediario: {}", intermediaryId);
                return false;
            }
            
            RuiIntermediary intermediary = intermediaryOpt.get();
            
            // Cambiar estado a TO_COMPLEMENT (que es el equivalente a 3 - POR COMPLEMENTAR)
            intermediary.setState(IntermediaryState.TO_COMPLEMENT);
            intermediaryRepository.save(intermediary);
            
            // Enviar correo electrónico de notificación
            sendComplementEmail(intermediary);
            
            log.info("Intermediario {} enviado a COMPLEMENTAR", intermediaryId);
            return true;
        } catch (Exception e) {
            log.error("Error al enviar a complementar el intermediario {}: {}", intermediaryId, e.getMessage(), e);
            return false;
        }
    }

    private void sendComplementEmail(RuiIntermediary intermediary) {
        try {
            // Obtener información del intermediario para la plantilla
            String name = getIntermediaryName(intermediary);
            String email = getIntermediaryEmail(intermediary);
            String address = getIntermediaryAddress(intermediary);
            String city = getIntermediaryCity(intermediary);
            String radicateNumber = intermediary.getRadicateNumber();
            String intermediaryType = intermediary.getTypeIntermediarieId().getValue();
            
            EmailTemplateDTO emailDTO = new EmailTemplateDTO();
            emailDTO.setTo(email);
            emailDTO.setSubject("Solicitud Complementar Información Registro Único de Intermediarios");
            emailDTO.setTemplate("email/complement-required.html");
            
            Map<String, String> templateVariables = new HashMap<>();
            templateVariables.put("name", name);
            templateVariables.put("address", address);
            templateVariables.put("city", city);
            templateVariables.put("radicateNumber", radicateNumber);
            templateVariables.put("intermediaryType", intermediaryType);
            templateVariables.put("currentDate", getCurrentFormattedDate());
            emailDTO.setTemplateVariables(templateVariables);
            
            // Enviar el correo utilizando el servicio de correo
            emailService.sendEmail(emailDTO);
            
            log.info("Correo de complementación enviado a {}", email);
        } catch (Exception e) {
            log.error("Error enviando correo de complementación: {}", e.getMessage(), e);
            // No lanzamos la excepción para no interrumpir el flujo del cambio de estado
        }
    }

    // Métodos auxiliares para extraer información del intermediario
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
            (intermediary.getCompanyId().getCityId() != null ? 
                intermediary.getCompanyId().getCityId().getName() : "") : 
            (intermediary.getPersonId().getCityId() != null ? 
                intermediary.getPersonId().getCityId().getName() : "");
    }

    private String getCurrentFormattedDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(new Date());
    }
    
    @Transactional
    public boolean aprobarRegistro(Long intermediaryId) {
        try {
            Optional<RuiIntermediary> intermediaryOpt = intermediaryRepository.findById(intermediaryId);
            
            if (intermediaryOpt.isEmpty()) {
                log.error("No se encontró el intermediario: {}", intermediaryId);
                return false;
            }
            
            RuiIntermediary intermediary = intermediaryOpt.get();
            
            // Cambiar estado a APPROVED
            intermediary.setState(IntermediaryState.APPROVED);
            intermediaryRepository.save(intermediary);
            
            // Enviar correo electrónico de aprobación
            sendApprovalEmail(intermediary);
            
            log.info("Intermediario {} APROBADO", intermediaryId);
            return true;
        } catch (Exception e) {
            log.error("Error al aprobar el intermediario {}: {}", intermediaryId, e.getMessage(), e);
            return false;
        }
    }

    private void sendApprovalEmail(RuiIntermediary intermediary) {
        try {
            // Obtener información del intermediario para la plantilla
            String name = getIntermediaryName(intermediary);
            String email = getIntermediaryEmail(intermediary);
            String address = getIntermediaryAddress(intermediary);
            String city = getIntermediaryCity(intermediary);
            String radicateNumber = intermediary.getRadicateNumber();
            String intermediaryType = intermediary.getTypeIntermediarieId().getValue();
            
            EmailTemplateDTO emailDTO = new EmailTemplateDTO();
            emailDTO.setTo(email);
            emailDTO.setSubject("Aprobación registro de intermediarios");
            emailDTO.setTemplate("email/approval.html");
            
            Map<String, String> templateVariables = new HashMap<>();
            templateVariables.put("name", name);
            templateVariables.put("address", address);
            templateVariables.put("city", city);
            templateVariables.put("radicateNumber", radicateNumber);
            templateVariables.put("intermediaryType", intermediaryType);
            templateVariables.put("currentDate", getCurrentFormattedDate());
            emailDTO.setTemplateVariables(templateVariables);
            
            // Enviar el correo
            emailService.sendEmail(emailDTO);
            
            log.info("Correo de aprobación enviado a {}", email);
        } catch (Exception e) {
            log.error("Error enviando correo de aprobación: {}", e.getMessage(), e);
        }
    }
    
    @Transactional
    public boolean regresarAFuncionario(Long intermediaryId) {
        try {
            Optional<RuiIntermediary> intermediaryOpt = intermediaryRepository.findById(intermediaryId);
            
            if (intermediaryOpt.isEmpty()) {
                log.error("No se encontró el intermediario: {}", intermediaryId);
                return false;
            }
            
            RuiIntermediary intermediary = intermediaryOpt.get();
            
            // Cambiar estado a DILIGENTED (volver a estado 2)
            intermediary.setState(IntermediaryState.DILIGENTED);
            intermediaryRepository.save(intermediary);
            
            // Enviar correo electrónico de cambio de estado
            sendStatusChangeEmail(intermediary);
            
            log.info("Intermediario {} regresado a FUNCIONARIO", intermediaryId);
            return true;
        } catch (Exception e) {
            log.error("Error al regresar a funcionario el intermediario {}: {}", intermediaryId, e.getMessage(), e);
            return false;
        }
    }

    private void sendStatusChangeEmail(RuiIntermediary intermediary) {
        try {
            // Obtener información del intermediario para la plantilla
            String name = getIntermediaryName(intermediary);
            String email = getIntermediaryEmail(intermediary);
            String address = getIntermediaryAddress(intermediary);
            String city = getIntermediaryCity(intermediary);
            String radicateNumber = intermediary.getRadicateNumber();
            String intermediaryType = intermediary.getTypeIntermediarieId().getValue();
            
            EmailTemplateDTO emailDTO = new EmailTemplateDTO();
            emailDTO.setTo(email);
            emailDTO.setSubject("Cambio de Estado en Solicitud");
            emailDTO.setTemplate("email/status-change.html");
            
            Map<String, String> templateVariables = new HashMap<>();
            templateVariables.put("name", name);
            templateVariables.put("address", address);
            templateVariables.put("city", city);
            templateVariables.put("radicateNumber", radicateNumber);
            templateVariables.put("intermediaryType", intermediaryType);
            templateVariables.put("currentDate", getCurrentFormattedDate());
            templateVariables.put("newStatus", "DILIGENCIADO");
            templateVariables.put("statusMessage", "Su solicitud ha sido regresada a un funcionario para revisión adicional.");
            emailDTO.setTemplateVariables(templateVariables);
            
            // Enviar el correo
            emailService.sendEmail(emailDTO);
            
            log.info("Correo de cambio de estado enviado a {}", email);
        } catch (Exception e) {
            log.error("Error enviando correo de cambio de estado: {}", e.getMessage(), e);
        }
    }
    
    private FirmaDigitalizadaDTO convertToDTO(RuiSupport support, Long intermediaryId) {
        if (support == null) {
            return null;
        }
        
        FirmaDigitalizadaDTO dto = new FirmaDigitalizadaDTO();
        dto.setId(support.getId());
        dto.setFilename(support.getFilename());
        dto.setRoute(support.getRoute());
        dto.setImagenUrl("/intermediary/firma/" + intermediaryId);  // Usar el ID del intermediario
        dto.setChecked(true);
        
        return dto;
    }
}
