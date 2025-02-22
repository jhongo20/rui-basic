package com.rui.basic.app.basic.web.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String description;
    private Integer status;
    private List<PermissionDTO> permissions;
    
    // Constructor para creación
    public RoleDto(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
