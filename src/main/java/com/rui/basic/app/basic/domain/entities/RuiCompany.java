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
@Table(name = "RUI_COMPANY")
@Data
@NoArgsConstructor
@ToString(exclude = {"intermediaries", "cityId", "departmentId"})
@EqualsAndHashCode(exclude = {"intermediaries", "cityId", "departmentId"})
public class RuiCompany implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rui_company_seq")
    @SequenceGenerator(name = "rui_company_seq", sequenceName = "RUI_COMPANY_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;
    
    @Column(name = "NIT", unique = true, length = 45)
    private String nit;
    
    @Column(name = "NAME", length = 183)
    private String name;
    
    @Column(name = "ADDRESS", length = 50)
    private String address;
    
    @Column(name = "EMAIL")
    private String email;
    
    @Column(name = "PHONE")
    private String phone;
        
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CITY_ID")
    private RuiCity cityId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENT_ID")
    private RuiDepartment departmentId;
    
    @OneToMany(mappedBy = "companyId", cascade = CascadeType.ALL)
    private Set<RuiIntermediary> intermediaries = new HashSet<>();
}
