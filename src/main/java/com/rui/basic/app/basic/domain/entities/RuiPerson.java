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
@Table(name = "RUI_PERSONS")
@Data
@NoArgsConstructor
@ToString(exclude = {"user", "intermediaries"})
@EqualsAndHashCode(exclude = {"user", "intermediaries"})
public class RuiPerson implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_PERSONS_SEQ")
    @SequenceGenerator(name = "RUI_PERSONS_SEQ", sequenceName = "RUI_PERSONS_SEQUENCE", allocationSize = 1)
    private Long id;
    
    @Column(name = "DOCUMENT_TYPE")
    private String documentType;
    
    @Column(name = "DOCUMENT_NUMBER")
    private String documentNumber;
    
    @Column(name = "FIRST_NAME")
    private String firstName;
    
    @Column(name = "SECOND_NAME")
    private String secondName;
    
    @Column(name = "FIRST_SURNAME")
    private String firstSurname;
    
    @Column(name = "SECOND_SURNAME")
    private String secondSurname;
    
    @Column(name = "ADDRESS")
    private String address;
    
    @Column(name = "EMAIL")
    private String email;
    
    @Column(name = "PHONE")
    private String phone;
    
    @Column(name = "CELLPHONE")
    private String cellphone;

    @OneToOne(mappedBy = "person", fetch = FetchType.LAZY)
    private RuiUser user;
    
    @OneToMany(mappedBy = "personId", cascade = CascadeType.ALL)
    private Set<RuiIntermediary> intermediaries = new HashSet<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CITY_ID")
    private RuiCity cityId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENT_ID")
    private RuiDepartment departmentId;
}
