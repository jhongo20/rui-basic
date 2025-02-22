package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "RUI_ACTIONS")
@Data
@NoArgsConstructor
@ToString(exclude = "permissions")
@EqualsAndHashCode(exclude = "permissions")
public class RuiActions implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_ACTIONS_SEQ")
    @SequenceGenerator(name = "RUI_ACTIONS_SEQ", sequenceName = "RUI_ACTIONS_SEQUENCE", allocationSize = 1)
    private Long id;
    
    @Column(name = "NAME")
    private String name;
    
    @Column(name = "DESCRIPTION")
    private String description;
    
    @Column(name = "STATUS")
    private Short status;
    
    @OneToMany(mappedBy = "actionId", cascade = CascadeType.ALL)
    private Set<RuiPermission> permissions = new HashSet<>();
}
