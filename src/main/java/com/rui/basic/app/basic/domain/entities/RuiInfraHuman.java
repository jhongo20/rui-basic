package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "RUI_INFRA_HUMAN")
@Data
@NoArgsConstructor
@ToString(exclude = {"intermediary", "workExperiences"})
@EqualsAndHashCode(exclude = {"intermediary", "workExperiences"})
public class RuiInfraHuman implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_INFRA_HUMAN_SEQ")
    @SequenceGenerator(name = "RUI_INFRA_HUMAN_SEQ", sequenceName = "RUI_INFRA_HUMAN_SEQUENCE", allocationSize = 1)
    private Long id;
    
    // Relaciones con personas
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROFESSIONAL_ID")
    private RuiPerson professionalId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEDIC_ID")
    private RuiPerson medicId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LAWYER_ID")
    private RuiPerson lawyerId;
    
    // IDs de soportes
    @Column(name = "PROFESSIONAL_SUPPORT_ID")
    private Long professionalSupportId;
    
    @Column(name = "MEDIC_SUPPORT_ID")
    private Long medicSupportId;
    
    // Fechas de licencias
    @Column(name = "MEDIC_LICENSE_DATE")
    @Temporal(TemporalType.DATE)
    private Date medicLicenseDate;
    
    @Column(name = "PROF_LICENSE_DATE")
    @Temporal(TemporalType.DATE)
    private Date profLicenseDate;
    
    // Relación con intermediario (bidireccional)
    @OneToOne(mappedBy = "infrastructureHumanId")
    private RuiIntermediary intermediary;
    
    // Relación con experiencias laborales
    @OneToMany(mappedBy = "infraHumanId", cascade = CascadeType.ALL)
    private Set<RuiWorkExperience> workExperiences = new HashSet<>();
}
