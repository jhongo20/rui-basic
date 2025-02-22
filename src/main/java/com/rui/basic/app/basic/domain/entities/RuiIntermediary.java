package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.rui.basic.app.basic.domain.enums.IntermediaryState;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "RUI_INTERMEDIARIES")
@Data
@NoArgsConstructor
@ToString(exclude = {"personId", "companyId", "assignments", "histories", "typeIntermediarieId", 
    "infrastructureHumanId", "infrastructureOperationalId", "ruiIdoniedadList"})
@EqualsAndHashCode(exclude = {"personId", "companyId", "assignments", "histories", 
    "typeIntermediarieId", "infrastructureHumanId", "infrastructureOperationalId", "ruiIdoniedadList"})
public class RuiIntermediary implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rui_intermediaries_seq")
    @SequenceGenerator(name = "rui_intermediaries_seq", sequenceName = "RUI_INTERMEDIARIES_SEQ", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    private Long id;
    
    @Column(name = "RADICATE_NUMBER", length = 100)
    private String radicateNumber;
    
    @Column(name = "RESOLUTION")
    private Long resolution;
    
    @Column(name = "STATE")
    @Enumerated(EnumType.ORDINAL)  // Usa ORDINAL porque el estado se guarda como número en la BD
    private IntermediaryState state;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TYPE_INTERMEDIARIE_ID")
    private RuiGenerics typeIntermediarieId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID")
    private RuiPerson personId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID")
    private RuiCompany companyId;
    
    @OneToMany(mappedBy = "intermediaryId", cascade = CascadeType.ALL)
    private Set<RuiAssignment> assignments = new HashSet<>();
    
    @OneToMany(mappedBy = "intermediaryId", cascade = CascadeType.ALL)
    private Set<RuiIntermediaryHistory> histories = new HashSet<>();
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "INFRASTRUCTURE_HUMAN_ID")
    private RuiInfraHuman infrastructureHumanId;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "OPERATIONAL_INFRA_ID")
    private RuiInfraOperational infrastructureOperationalId;
    
    @OneToMany(mappedBy = "intermediaryId")
    private List<RuiIdoniedad> ruiIdoniedadList;

    @OneToMany(mappedBy = "intermediaryId", cascade = CascadeType.ALL)
    private List<RuiIntermediaryHistory> ruiIntermediaryHistoryList;
    
    
    // Métodos helper para manejar las relaciones bidireccionales
    
    public void addAssignment(RuiAssignment assignment) {
        assignments.add(assignment);
        assignment.setIntermediaryId(this);
    }
    
    public void removeAssignment(RuiAssignment assignment) {
        assignments.remove(assignment);
        assignment.setIntermediaryId(null);
    }
    
    public void addHistory(RuiIntermediaryHistory history) {
        histories.add(history);
        history.setIntermediaryId(this);
    }
    
    public void removeHistory(RuiIntermediaryHistory history) {
        histories.remove(history);
        history.setIntermediaryId(null);
    }

    //---
    @OneToMany(mappedBy = "intermediaryId", fetch = FetchType.LAZY)
private List<RuiIdoniedad> idoneidadList;


}