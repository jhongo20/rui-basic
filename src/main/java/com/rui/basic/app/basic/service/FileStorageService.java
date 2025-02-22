package com.rui.basic.app.basic.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.rui.basic.app.basic.exception.FileStorageException;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class FileStorageService {
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    
    private final Path fileStorageLocation;
    
    @Value("${app.file.upload-dir}")
    private String uploadDir;

    public FileStorageService(@Value("${app.file.upload-dir}") String uploadDir) {
        this.uploadDir = uploadDir;
        this.fileStorageLocation = Paths.get(uploadDir)
            .toAbsolutePath().normalize();
        init();
    }
    
    private void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("No se pudo crear el directorio donde se almacenarán los archivos.", ex);
        }
    }
    
    public String storeFile(MultipartFile file, String subDirectory, Long userId, Long intermediaryId) {
        try {
            // Validar nombre de archivo
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            if(fileName.contains("..")) {
                throw new FileStorageException("El nombre del archivo contiene una ruta inválida " + fileName);
            }
            
            // Crear estructura de directorios
            Path targetLocation = this.fileStorageLocation.resolve(
                Paths.get(userId.toString(), 
                         intermediaryId.toString(), 
                         subDirectory)
            ).normalize();
            
            Files.createDirectories(targetLocation);
            
            // Generar nombre único
            String newFileName = generateUniqueFileName(fileName);
            Path filePath = targetLocation.resolve(newFileName);
            
            // Copiar archivo
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return filePath.toString();
        } catch (IOException ex) {
            throw new FileStorageException("No se pudo almacenar el archivo " + file.getOriginalFilename(), ex);
        }
    }
    
    private String generateUniqueFileName(String originalFileName) {
        String extension = FilenameUtils.getExtension(originalFileName);
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;
    }
    
    public Resource loadFileAsResource(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("Archivo no encontrado " + filePath);
            }
        } catch (MalformedURLException | FileNotFoundException ex) {
            throw new FileStorageException("Archivo no encontrado " + filePath, ex);
        }
    }
    
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new FileStorageException("No se pudo eliminar el archivo " + filePath, ex);
        }
    }
}