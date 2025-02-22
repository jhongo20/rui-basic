package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "RUI_ASSIGNMENTS")
@Data
@NoArgsConstructor
@ToString(exclude = {"intermediaryId", "userId"})
@EqualsAndHashCode(exclude = {"intermediaryId", "userId"})
public class RuiAssignment implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_ASSIGNMENTS_SEQ")
    @SequenceGenerator(name = "RUI_ASSIGNMENTS_SEQ", sequenceName = "RUI_ASSIGNMENTS_SEQUENCE", allocationSize = 1)
    private Long id;
    
    @Column(name = "ASSIGNMENT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date assignmentDate;
    
    @Column(name = "STATUS")
    private Integer status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INTERMEDIARY_ID")
    private RuiIntermediary intermediaryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private RuiUser userId;
}
