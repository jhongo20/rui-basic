package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "RUI_SUPPORT")
@Data
@NoArgsConstructor
@ToString(exclude = {"idoniedadId", "infraHumnaId", "infraOperationalProce", "infraOperationalSoft", 
                    "infraOperationalHard", "workExperienceId", "infraOperationalCc", "infraOperationalSign", 
                    "infraOperationalSsl"})
@EqualsAndHashCode(exclude = {"idoniedadId", "infraHumnaId", "infraOperationalProce", "infraOperationalSoft", 
                              "infraOperationalHard", "workExperienceId", "infraOperationalCc", "infraOperationalSign", 
                              "infraOperationalSsl"})
public class RuiSupport implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_SUPPORT_SEQ")
    @SequenceGenerator(name = "RUI_SUPPORT_SEQ", sequenceName = "RUI_SUPPORT_SEQUENCE", allocationSize = 1)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDONIEDAD_ID")
    private RuiIdoniedad idoniedadId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INFRA_HUMNA_ID")
    private RuiInfraHuman infraHumnaId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INFRA_OPERATIONAL_PROCE")
    private RuiInfraOperational infraOperationalProce;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INFRA_OPERATIONAL_SOFT")
    private RuiInfraOperational infraOperationalSoft;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INFRA_OPERATIONAL_HARD")
    private RuiInfraOperational infraOperationalHard;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORK_EXPERIENCE_ID")
    private RuiWorkExperience workExperienceId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INFRA_OPERATIONAL_CC")
    private RuiInfraOperational infraOperationalCc;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INFRA_OPERATIONAL_SIGN")
    private RuiInfraOperational infraOperationalSign;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INFRA_OPERATIONAL_SSL")
    private RuiInfraOperational infraOperationalSsl;
    
    @Column(name = "FILENAME")
    private String filename;
    
    @Column(name = "ROUTE")
    private String route;
    
    @Column(name = "EXTENCION")
    private String extencion;
    
    @Column(name = "STATUS")
    private Short status;
}
