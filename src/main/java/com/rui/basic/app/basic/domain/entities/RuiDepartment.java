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
@Table(name = "RUI_DEPARTMENTS")
@Data
@NoArgsConstructor
@ToString(exclude = "cities")
@EqualsAndHashCode(exclude = "cities")
public class RuiDepartment implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_DEPARTMENTS_SEQ")
    @SequenceGenerator(name = "RUI_DEPARTMENTS_SEQ", sequenceName = "RUI_DEPARTMENTS_SEQUENCE", allocationSize = 1)
    private Long id;
    
    @Column(name = "NAME")
    private String name;
  
    @OneToMany(mappedBy = "departmentId", cascade = CascadeType.ALL)
    private Set<RuiCity> cities = new HashSet<>();
}
