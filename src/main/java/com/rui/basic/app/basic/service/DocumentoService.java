package com.rui.basic.app.basic.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import com.rui.basic.app.basic.domain.entities.RuiIdoniedad;
import com.rui.basic.app.basic.domain.entities.RuiInfraHuman;
import com.rui.basic.app.basic.domain.entities.RuiInfraOperational;
import com.rui.basic.app.basic.domain.entities.RuiIntermediary;
import com.rui.basic.app.basic.domain.entities.RuiPerson;
import com.rui.basic.app.basic.domain.entities.RuiSupport;
import com.rui.basic.app.basic.domain.entities.RuiWorkExperience;
import com.rui.basic.app.basic.repository.RuiIdoniedadRepository;
import com.rui.basic.app.basic.repository.RuiInfraHumanRepository;
import com.rui.basic.app.basic.repository.RuiInfraOperationalRepository;
import com.rui.basic.app.basic.repository.RuiIntermediaryRepository;
import com.rui.basic.app.basic.repository.RuiSupportRepository;
import com.rui.basic.app.basic.repository.RuiWorkExperienceRepository;


@Service
public class DocumentoService {
    
    private static final Logger log = LoggerFactory.getLogger(DocumentoService.class);
    
    private final Path documentosLocation;
    private final RuiIdoniedadRepository idoniedadRepository;
    private final RuiWorkExperienceRepository workExperienceRepository;
    private final RuiSupportRepository ruiSupportRepository;
    private final RuiIntermediaryRepository intermediaryRepository;
    private final RuiInfraOperationalRepository infraOperativaRepository;
    private final RuiInfraHumanRepository infraHumanaRepository;
    
    @Value("${app.documentos.ruta:/tmp/documentos}")
    private String baseDocumentosRuta;
    
    public DocumentoService(
            @Value("${app.documentos.ruta:/tmp/documentos}") String documentosRuta,
            RuiIdoniedadRepository idoniedadRepository, 
            RuiWorkExperienceRepository workExperienceRepository,
            RuiSupportRepository ruiSupportRepository,
            RuiIntermediaryRepository intermediaryRepository,
            RuiInfraOperationalRepository infraOperativaRepository,
            RuiInfraHumanRepository infraHumanaRepository) {
        this.documentosLocation = Paths.get(documentosRuta);
        this.idoniedadRepository = idoniedadRepository;
        this.workExperienceRepository = workExperienceRepository;
        this.ruiSupportRepository = ruiSupportRepository;
        this.intermediaryRepository = intermediaryRepository;
        this.infraOperativaRepository = infraOperativaRepository;
        this.infraHumanaRepository = infraHumanaRepository;
        
        // Crear directorio si no existe
        try {
            Files.createDirectories(this.documentosLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el directorio de almacenamiento de documentos", e);
        }
    }

    public Resource loadIdoneidadDocument(Long id) {
    try {
        log.info("Buscando documento de idoneidad con ID: {}", id);
        
        // 1. Buscar la idoneidad
        RuiIdoniedad idoniedad = idoniedadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Idoneidad no encontrada para ID: " + id));

        // 2. Verificar que la persona esté activa
        if (idoniedad.getPersonId() == null) {
            throw new RuntimeException("No hay persona asociada a esta idoneidad");
        }

        if (!idoniedad.getPersonId().isActive()) {
            throw new RuntimeException("La persona asociada a esta idoneidad no está activa");
        }
        
        // 3. Buscar el soporte (pasando el estado activo)
        RuiSupport support = ruiSupportRepository
            .findByIdoniedadIdAndPersonStatus(idoniedad, RuiPerson.Status.ACTIVE.getValue())
            .orElseThrow(() -> new RuntimeException("Soporte no encontrado para la idoneidad con ID: " + id));
        
        log.info("Soporte encontrado: ID: {}, Filename: {}, Route: {}", 
                support.getId(), support.getFilename(), support.getRoute());
        
        // 4. Construir la ruta del archivo
        String filePath = support.getRoute() + "/" + support.getFilename();
        log.info("Intentando acceder al archivo en: {}", filePath);
        
        // 5. Verificar si el archivo existe
        File file = new File(filePath);
        if (!file.exists()) {
            log.error("El archivo no existe en la ruta: {}", filePath);
            throw new RuntimeException("El archivo no existe en la ruta especificada");
        }
        
        // 6. Cargar el recurso
        Path path = Paths.get(filePath);
        Resource resource = new UrlResource(path.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            log.info("Recurso cargado correctamente desde: {}", filePath);
            return resource;
        } else {
            log.error("No se pudo leer el archivo aunque existe en el sistema de archivos: {}", filePath);
            throw new RuntimeException("No se pudo leer el archivo");
        }
        
    } catch (Exception e) {
        log.error("Error al cargar documento de idoneidad: {}", e.getMessage(), e);
        throw new RuntimeException("Error al cargar documento de idoneidad: " + e.getMessage());
    }
}
    
    /**
     * Genera diferentes variaciones de rutas posibles para encontrar el archivo
     */
    private List<String> generateAlternativePaths(RuiSupport support, RuiIdoniedad idoniedad) {
        List<String> paths = new ArrayList<>();
        
        try {
            Long userId = 1L; // Valor por defecto o configurable
            Long intermediaryId = idoniedad.getIntermediaryId().getId();
            Long personId = idoniedad.getPersonId().getId();
            
            // Basado en el código antiguo, construir varias posibles rutas
            
            // 1. Ruta base + original
            paths.add(baseDocumentosRuta + "/" + support.getRoute() + "/" + support.getFilename());
            
            // 2. Solo la ruta registrada (ya verificada antes)
            paths.add(support.getRoute() + "/" + support.getFilename());
            
            // 3. Rutas basadas en la estructura antigua
            RuiIntermediary intermediary = intermediaryRepository.findById(intermediaryId).orElse(null);
            if (intermediary != null) {
                Long typeId = intermediary.getTypeIntermediarieId().getId();
                
                // Estructura para agente (typeId = 4)
                if (typeId == 4L) {
                    String path = baseDocumentosRuta + "/" + userId + "/" + intermediaryId + 
                                 "/IDO_PROFESIONAL/" + personId + "/" + idoniedad.getId();
                    paths.add(path + "/" + support.getFilename());
                } else {
                    String path = baseDocumentosRuta + "/" + userId + "/" + intermediaryId + 
                                 "/IDO_PROFESIONAL/" + personId;
                    paths.add(path + "/" + support.getFilename());
                }
            }
            
            // 4. Probar con nombres de archivo alternativos
            paths.add(support.getRoute() + "/idoneidad_" + idoniedad.getId() + ".pdf");
            paths.add(baseDocumentosRuta + "/idoneidad_" + idoniedad.getId() + ".pdf");
            
        } catch (Exception e) {
            log.warn("Error generando rutas alternativas", e);
        }
        
        for (String path : paths) {
            log.debug("Ruta alternativa generada: {}", path);
        }
        
        return paths;
    }
    
    public Resource loadInfraHumanaDocument(Long id) {
        log.info("Iniciando carga de documento de infraestructura humana con ID: {}", id);
        
        try {
            // 1. Buscar la infraestructura humana
            log.debug("Buscando infraestructura humana en base de datos - ID: {}", id);
            RuiInfraHuman infraHumana = infraHumanaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Infraestructura humana no encontrada para ID: " + id));
            log.debug("Infraestructura humana encontrada - ID: {}, Professional ID: {}", 
                infraHumana.getId(), 
                infraHumana.getProfessionalId() != null ? infraHumana.getProfessionalId().getId() : "null");
            
            // 2. Buscar el soporte
            log.debug("Buscando soporte para infraestructura humana ID: {}", id);
            RuiSupport support = ruiSupportRepository.findByInfraHumnaId(infraHumana)
                    .orElseThrow(() -> new RuntimeException("No se encontró documento para la infraestructura humana"));
            log.debug("Soporte encontrado - ID: {}, Filename: {}, Route: {}, Extension: {}", 
                support.getId(), 
                support.getFilename(), 
                support.getRoute(),
                support.getExtencion());
            
            // 3. Construir la ruta del archivo
            String filePath = support.getRoute() + "/" + support.getFilename();
            log.info("Ruta completa del archivo: {}", filePath);
            
            // 4. Verificar si el archivo existe
            File file = new File(filePath);
            if (!file.exists()) {
                log.error("El archivo no existe en la ruta especificada: {}", filePath);
                log.debug("Verificando permisos del directorio: {}", support.getRoute());
                log.debug("Directorio existe: {}, Es directorio: {}, Puede leer: {}", 
                    new File(support.getRoute()).exists(),
                    new File(support.getRoute()).isDirectory(),
                    new File(support.getRoute()).canRead());
                throw new RuntimeException("El archivo no existe en la ruta especificada");
            }
            log.debug("Archivo encontrado en el sistema de archivos - Tamaño: {} bytes", file.length());
            
            // 5. Cargar el recurso
            Path path = Paths.get(filePath);
            log.debug("Creando recurso URL para la ruta: {}", path.toUri());
            Resource resource = new UrlResource(path.toUri());
            
            // 6. Verificar el recurso
            if (resource.exists()) {
                log.debug("Recurso existe y es legible - Tamaño: {} bytes", resource.contentLength());
                if (resource.isReadable()) {
                    log.info("Recurso cargado exitosamente - URI: {}", resource.getURI());
                    return resource;
                } else {
                    log.error("El recurso existe pero no es legible: {}", filePath);
                    throw new RuntimeException("El archivo existe pero no es legible");
                }
            } else {
                log.error("No se pudo crear el recurso para el archivo: {}", filePath);
                throw new RuntimeException("No se pudo crear el recurso para el archivo");
            }
            
        } catch (Exception e) {
            log.error("Error al cargar documento de infraestructura humana - ID: {} - Error: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error al cargar documento de infraestructura humana: " + e.getMessage(), e);
        }
    }
    
    public Resource loadWorkExperienceDocument(Long id) {
        log.info("Iniciando carga de documento de experiencia laboral con ID: {}", id);
        
        try {
            // 1. Buscar la experiencia laboral
            RuiWorkExperience workExperience = workExperienceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Experiencia laboral no encontrada para ID: " + id));
            
            // 2. Buscar el soporte (único)
            RuiSupport support = ruiSupportRepository.findFirstByWorkExperienceId(workExperience)
                    .orElseThrow(() -> new RuntimeException("No se encontró documento para la experiencia laboral"));
            
            log.debug("Soporte encontrado - ID: {}, Filename: {}, Route: {}", 
                support.getId(), 
                support.getFilename(), 
                support.getRoute());
            
            // 3. Construir la ruta del archivo
            String filePath = support.getRoute() + "/" + support.getFilename();
            log.info("Ruta completa del archivo: {}", filePath);
            
            // 4. Verificar si el archivo existe
            File file = new File(filePath);
            if (!file.exists()) {
                log.error("El archivo no existe en la ruta especificada: {}", filePath);
                throw new RuntimeException("El archivo no existe en la ruta especificada");
            }
            
            // 5. Cargar el recurso
            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                log.info("Recurso cargado exitosamente - URI: {}", resource.getURI());
                return resource;
            } else {
                log.error("No se pudo leer el archivo aunque existe en el sistema de archivos: {}", filePath);
                throw new RuntimeException("No se pudo leer el archivo");
            }
            
        } catch (Exception e) {
            log.error("Error al cargar documento de experiencia laboral - ID: {} - Error: {}", 
                id, e.getMessage(), e);
            throw new RuntimeException("Error al cargar documento de experiencia laboral: " + e.getMessage());
        }
    }

    public Resource loadInfraOperativaDocument(Long id, String tipo) {
        try {
            // Obtener la infraestructura operativa
            RuiInfraOperational infraOperativa = infraOperativaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Infraestructura operativa no encontrada para ID: " + id));
            
            // Determinar qué campo buscar según el tipo
            List<RuiSupport> supports;
            
            switch (tipo.toLowerCase()) {
                case "camara":
                    supports = ruiSupportRepository.findAllByInfraOperationalCc(infraOperativa);
                    break;
                case "software":
                    supports = ruiSupportRepository.findAllByInfraOperationalSoft(infraOperativa);
                    break;
                case "equipos":
                    supports = ruiSupportRepository.findAllByInfraOperationalHard(infraOperativa);
                    break;
                case "procesamiento":
                    supports = ruiSupportRepository.findAllByInfraOperationalProce(infraOperativa);
                    break;
                case "ssl":
                    supports = ruiSupportRepository.findAllByInfraOperationalSsl(infraOperativa);
                    break;
                default:
                    throw new RuntimeException("Tipo de documento no reconocido: " + tipo);
            }
            
            if (supports.isEmpty()) {
                log.error("Soporte no encontrado para la infraestructura operativa con ID: {} y tipo: {}", id, tipo);
                throw new RuntimeException("Soporte no encontrado para la infraestructura operativa");
            }
            
            // Seleccionar el soporte más reciente con status=1
            RuiSupport support = supports.stream()
                    .filter(s -> s.getStatus() == 1) // Filtrar solo soportes activos
                    .max(Comparator.comparing(RuiSupport::getId)) // Tomar el más reciente por ID
                    .orElseThrow(() -> new RuntimeException("No se encontró soporte activo para el tipo: " + tipo));
            
            log.info("Soporte encontrado: ID: {}, Filename: {}, Route: {}", 
                    support.getId(), support.getFilename(), support.getRoute());
            
            // Construir la ruta completa al archivo
            String filePath = support.getRoute() + "/" + support.getFilename();
            log.info("Intentando acceder al archivo en: {}", filePath);
            
            // Verificar si el archivo existe
            File file = new File(filePath);
            if (!file.exists()) {
                log.warn("El archivo no existe en la ruta original: {}", filePath);
                throw new RuntimeException("El archivo no existe en la ruta especificada");
            }
            
            // Cargar el recurso
            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                log.info("Recurso cargado correctamente desde: {}", filePath);
                return resource;
            } else {
                log.error("No se pudo leer el archivo aunque existe en el sistema de archivos: {}", filePath);
                throw new RuntimeException("No se pudo leer el archivo");
            }
        } catch (MalformedURLException e) {
            log.error("Error URL malformada: {}", e.getMessage(), e);
            throw new RuntimeException("Error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error al cargar documento de infraestructura operativa: {}", e.getMessage(), e);
            throw new RuntimeException("Error al cargar documento de infraestructura operativa", e);
        }
    }


}