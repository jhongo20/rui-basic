package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "RUI_CITIES")
@Data
@NoArgsConstructor
@ToString(exclude = "departmentId")
@EqualsAndHashCode(exclude = "departmentId")
public class RuiCity implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_CITIES_SEQ")
    @SequenceGenerator(name = "RUI_CITIES_SEQ", sequenceName = "RUI_CITIES_SEQUENCE", allocationSize = 1)
    private Long id;
    
    @Column(name = "NAME")
    private String name;
    
    /*@Column(name = "CODE")
    private String code;
    
    @Column(name = "STATUS")
    private Short status;*/
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENT_ID")
    private RuiDepartment departmentId;
}
