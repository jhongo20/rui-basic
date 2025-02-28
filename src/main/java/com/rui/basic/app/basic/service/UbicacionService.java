package com.rui.basic.app.basic.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.rui.basic.app.basic.domain.entities.RuiCity;
import com.rui.basic.app.basic.domain.entities.RuiDepartment;
import com.rui.basic.app.basic.repository.RuiCityRepository;
import com.rui.basic.app.basic.repository.RuiDepartmentRepository;
import com.rui.basic.app.basic.web.dto.CityDTO;
import com.rui.basic.app.basic.web.dto.DepartmentDTO;

@Service
public class UbicacionService {
    
    private final RuiDepartmentRepository departmentRepository;
    private final RuiCityRepository cityRepository;
    
    public UbicacionService(RuiDepartmentRepository departmentRepository, RuiCityRepository cityRepository) {
        this.departmentRepository = departmentRepository;
        this.cityRepository = cityRepository;
    }
    
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAllByOrderByNameAsc().stream()
                .map(this::convertToDepartmentDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<DepartmentDTO> getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .map(this::convertToDepartmentDTO);
    }

    // Nuevo método para obtener todas las ciudades
    public List<CityDTO> getAllCities() {
        return cityRepository.findAllByOrderByNameAsc().stream()
                .map(this::convertToCityDTO)
                .collect(Collectors.toList());
    }
    
    public List<CityDTO> getCitiesByDepartmentId(Long departmentId) {
        return cityRepository.findByDepartmentId_IdOrderByNameAsc(departmentId).stream()
                .map(this::convertToCityDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<CityDTO> getCityById(Long id) {
        return cityRepository.findById(id)
                .map(this::convertToCityDTO);
    }
    
    // Métodos de conversión de entidad a DTO
    private DepartmentDTO convertToDepartmentDTO(RuiDepartment department) {
        return DepartmentDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .build();
    }
    
    private CityDTO convertToCityDTO(RuiCity city) {
        return CityDTO.builder()
                .id(city.getId())
                .name(city.getName())
                .department(convertToDepartmentDTO(city.getDepartmentId()))
                .build();
    }
}