package com.rui.basic.app.basic.service;

import com.rui.basic.app.basic.domain.entities.RuiIntermediary;
import com.rui.basic.app.basic.domain.entities.RuiSupport;
import com.rui.basic.app.basic.domain.enums.IntermediaryState;
import com.rui.basic.app.basic.repository.RuiIntermediaryRepository;
import com.rui.basic.app.basic.repository.RuiSupportRepository;
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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FirmaDigitalizadaService {

    private static final Logger log = LoggerFactory.getLogger(FirmaDigitalizadaService.class);
    private final RuiSupportRepository supportRepository;
    private final RuiIntermediaryRepository intermediaryRepository;
    private final Path firmasStorageLocation;

   
    public FirmaDigitalizadaService(
            RuiSupportRepository supportRepository,
            RuiIntermediaryRepository intermediaryRepository,
            @Value("${app.documentos.ruta:/tmp/documentos}") String documentosRuta) {
        this.supportRepository = supportRepository;
        this.intermediaryRepository = intermediaryRepository;
        
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
            
            Optional<RuiSupport> supportOpt = supportRepository.findByInfraOperationalSign(intermediary.getInfrastructureOperationalId());
            
            if (supportOpt.isEmpty()) {
                log.info("No se encontró firma digitalizada para el intermediario: {}", intermediaryId);
                return null;
            }
            
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
            Optional<RuiSupport> supportOpt = supportRepository.findByInfraOperationalSign(intermediary.getInfrastructureOperationalId());
            
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
    public boolean marcarComoRevisado(Long intermediaryId) {
        try {
            Optional<RuiIntermediary> intermediaryOpt = intermediaryRepository.findById(intermediaryId);
            
            if (intermediaryOpt.isEmpty()) {
                log.error("No se encontró el intermediario: {}", intermediaryId);
                return false;
            }
            
            RuiIntermediary intermediary = intermediaryOpt.get();
            intermediary.setState(IntermediaryState.REVIEWED);
            intermediaryRepository.save(intermediary);
            
            log.info("Intermediario {} marcado como REVISADO", intermediaryId);
            return true;
        } catch (Exception e) {
            log.error("Error al marcar como revisado el intermediario {}: {}", intermediaryId, e.getMessage(), e);
            return false;
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
            intermediary.setState(IntermediaryState.COMPLEMENTED);
            intermediaryRepository.save(intermediary);
            
            log.info("Intermediario {} enviado a COMPLEMENTAR", intermediaryId);
            return true;
        } catch (Exception e) {
            log.error("Error al enviar a complementar el intermediario {}: {}", intermediaryId, e.getMessage(), e);
            return false;
        }
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
            intermediary.setState(IntermediaryState.APPROVED);
            intermediaryRepository.save(intermediary);
            
            log.info("Intermediario {} APROBADO", intermediaryId);
            return true;
        } catch (Exception e) {
            log.error("Error al aprobar el intermediario {}: {}", intermediaryId, e.getMessage(), e);
            return false;
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
            intermediary.setState(IntermediaryState.RENEWAL);
            intermediaryRepository.save(intermediary);
            
            log.info("Intermediario {} regresado a FUNCIONARIO", intermediaryId);
            return true;
        } catch (Exception e) {
            log.error("Error al regresar a funcionario el intermediario {}: {}", intermediaryId, e.getMessage(), e);
            return false;
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
