package com.rui.basic.app.basic.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rui.basic.app.basic.domain.entities.RuiRoles;
import com.rui.basic.app.basic.exception.ResourceNotFoundException;
import com.rui.basic.app.basic.repository.RuiRolesRepository;
import com.rui.basic.app.basic.web.dto.RoleDto;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class RoleService {
    
    @Autowired
    private RuiRolesRepository roleRepository;
    
    public List<RuiRoles> getAllRoles() {
        return roleRepository.findAll();
    }
    
    public RuiRoles getRoleById(Long id) {
        return roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));
    }
    
    public RuiRoles createRole(RoleDto roleDto) {
        RuiRoles role = new RuiRoles();
        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());
        role.setStatus(1); // activo por defecto
        return roleRepository.save(role);
    }
    
    public RuiRoles updateRole(Long id, RoleDto roleDto) {
        RuiRoles role = getRoleById(id);
        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());
        return roleRepository.save(role);
    }
    
    public void deleteRole(Long id) {
        RuiRoles role = getRoleById(id);
        role.setStatus(0); // soft delete
        roleRepository.save(role);
    }
}
