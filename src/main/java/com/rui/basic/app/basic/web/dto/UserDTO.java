package com.rui.basic.app.basic.web.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String username;
    private RoleDto role;
    private PersonDTO person;
    private Short status;
    private Short assigned;

    // Constructor para la proyección JPA
    public UserDTO(Long id, String username) {
        this.id = id;
        this.username = username;
    }
}
