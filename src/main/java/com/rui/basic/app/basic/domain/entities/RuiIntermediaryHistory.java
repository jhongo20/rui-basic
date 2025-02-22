package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.rui.basic.app.basic.domain.enums.IntermediaryState;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "RUI_INTERMEDIARY_HISTORY")
@Data
@NoArgsConstructor
@ToString(exclude = {"intermediaryId", "historyDetails", "functionaryId"})
@EqualsAndHashCode(exclude = {"intermediaryId", "historyDetails", "functionaryId"})
@NamedQueries({
    @NamedQuery(name = "RuiIntermediaryHistory.findAll", 
        query = "SELECT r FROM RuiIntermediaryHistory r"),
    @NamedQuery(name = "find.intermediary.history", 
        query = "SELECT r FROM RuiIntermediaryHistory r WHERE r.intermediaryId.id = :intermediaryId AND r.status = :status")
})
public class RuiIntermediaryHistory implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_HISTORY_SEQ")
    @SequenceGenerator(name = "RUI_HISTORY_SEQ", sequenceName = "RUI_HISTORY_SEQUENCE", allocationSize = 1)
    private Long id;
    
    @Column(name = "OBSERVATION")
    private String observation;
    
    @Column(name = "STATUS")
    private Short status;
    
    @Column(name = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datetime;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INTERMEDIARY_ID")
    private RuiIntermediary intermediaryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FUNCTIONARY_ID")
    private RuiUser functionaryId;
    
    @OneToMany(mappedBy = "intermediaryHistoryId", cascade = CascadeType.ALL)
    private Set<RuiHistoryDetails> historyDetails = new HashSet<>();

    // Métodos de conveniencia para mantener la sincronización bidireccional
    public void addHistoryDetail(RuiHistoryDetails detail) {
        historyDetails.add(detail);
        detail.setIntermediaryHistoryId(this);
    }

    public void removeHistoryDetail(RuiHistoryDetails detail) {
        historyDetails.remove(detail);
        detail.setIntermediaryHistoryId(null);
    }

    // Método para establecer el estado usando el enum
    public void setIntermediaryState(IntermediaryState state) {
        this.status = state.getState().shortValue();
    }

    // Método para obtener el estado como enum
    public IntermediaryState getIntermediaryState() {
        return IntermediaryState.fromState(this.status.intValue());
    }
}
