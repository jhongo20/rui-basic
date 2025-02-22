package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "RUI_IDONIEDAD")
@Data
@NoArgsConstructor
@ToString(exclude = {"intermediaryId", "personId"})
@EqualsAndHashCode(exclude = {"intermediaryId", "personId"})
public class RuiIdoniedad implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_IDONIEDAD_SEQ")
    @SequenceGenerator(name = "RUI_IDONIEDAD_SEQ", sequenceName = "RUI_IDONIEDAD_SEQUENCE", allocationSize = 1)
    private Long id;
    
    @Column(name = "COURSE_ENTITY")
    private String courseEntity;
    
    @Column(name = "DATE_COURSE")
    @Temporal(TemporalType.DATE)
    private Date dateCourse;
    
    @Column(name = "STATUS")
    private Short status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INTERMEDIARY_ID")
    private RuiIntermediary intermediaryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID")
    private RuiPerson personId;
}
