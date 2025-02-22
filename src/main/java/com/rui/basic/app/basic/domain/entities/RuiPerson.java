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

    @Column(name = "STATUS")
    private Integer status;  // 1: Activo, 0: Inactivo, 2: Eliminado

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

    // Optional: Agregar enum para manejar los estados
    public enum Status {
        INACTIVE(0),
        ACTIVE(1),
        DELETED(2);

        private final int value;

        Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Status fromValue(int value) {
            for (Status status : Status.values()) {
                if (status.value == value) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid status value: " + value);
        }
    }

    // Métodos de conveniencia para verificar el estado
    public boolean isActive() {
        return Status.ACTIVE.getValue() == this.status;
    }

    public boolean isInactive() {
        return Status.INACTIVE.getValue() == this.status;
    }

    public boolean isDeleted() {
        return Status.DELETED.getValue() == this.status;
    }
}
