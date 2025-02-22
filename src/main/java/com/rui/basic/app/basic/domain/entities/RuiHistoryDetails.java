package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "RUI_HISTORY_DETAILS")
@Data
@NoArgsConstructor
@ToString(exclude = "intermediaryHistoryId")
@EqualsAndHashCode(exclude = "intermediaryHistoryId")
public class RuiHistoryDetails implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_HISTORY_DETAILS_SEQ")
    @SequenceGenerator(name = "RUI_HISTORY_DETAILS_SEQ", sequenceName = "RUI_HISTORY_DETAILS_SEQUENCE", allocationSize = 1)
    private Long id;
    
    @Column(name = "FIELD_NAME")
    private String fieldName;
    
    @Column(name = "OBSERVATION")
    private String observation;
    
    @Column(name = "TABLE_NAME")
    private String tableName;
    
    @Column(name = "TABLE_ID")
    private Long tableId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INTERMEDIARY_HISTORY_ID")
    private RuiIntermediaryHistory intermediaryHistoryId;
}
