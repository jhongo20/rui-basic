package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "RUI_ROLES")
@Data
@NoArgsConstructor
@ToString(exclude = "users")
@EqualsAndHashCode(exclude = "users")
public class RuiRoles implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_ROLES_SEQ")
    @SequenceGenerator(name = "RUI_ROLES_SEQ", sequenceName = "RUI_ROLES_SEQUENCE", allocationSize = 1)
    private Long id;  // Cambiado de BigDecimal a Long
    
    @Column(name = "NAME", length = 100, unique = true)
    private String name;
    
    @Column(name = "DESCRIPTION", length = 100)
    private String description;
    
    @Column(name = "STATUS")
    private Integer status;
    
    @OneToMany(mappedBy = "roleId", fetch = FetchType.LAZY)
    private Set<RuiUser> users = new HashSet<>();
    
    @OneToMany(mappedBy = "roleId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<RuiPermission> permissions = new HashSet<>();
    
    // Getters y setters
}
