package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "RUI_TRAININGS")
@Data
@NoArgsConstructor
@ToString(exclude = "intermediaryId")
@EqualsAndHashCode(exclude = "intermediaryId")
public class RuiTrainings implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_TRAININGS_SEQ")
    @SequenceGenerator(name = "RUI_TRAININGS_SEQ", sequenceName = "RUI_TRAININGS_SEQUENCE", allocationSize = 1)
    private Long id;
    
    @Column(name = "TRAINING_TYPE")
    private String trainingType;
    
    @Column(name = "INSTITUTION")
    private String institution;
    
    @Column(name = "TITLE")
    private String title;
    
    @Column(name = "START_DATE")
    @Temporal(TemporalType.DATE)
    private Date startDate;
    
    @Column(name = "END_DATE")
    @Temporal(TemporalType.DATE)
    private Date endDate;
    
    @Column(name = "STATUS")
    private Short status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INTERMEDIARY_ID")
    private RuiIntermediary intermediaryId;
}
