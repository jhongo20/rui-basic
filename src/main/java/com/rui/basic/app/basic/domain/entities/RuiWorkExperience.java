package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "RUI_WORK_EXPERIENCE")
@Data
@NoArgsConstructor
@ToString(exclude = "infraHumanId")
@EqualsAndHashCode(exclude = "infraHumanId")
public class RuiWorkExperience implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_WORK_EXP_SEQ")
    @SequenceGenerator(name = "RUI_WORK_EXP_SEQ", sequenceName = "RUI_WORK_EXP_SEQUENCE", allocationSize = 1)
    private Long id;
    
    @Column(name = "COMPANY")
    private String company;
    
    @Column(name = "CHARGE")
    private String charge;
    
    @Column(name = "NAME_BOSS")
    private String nameBoss;
    
    @Column(name = "PHONE_BOSS")
    private String phoneBoss;
    
    @Column(name = "START_DATE")
    @Temporal(TemporalType.DATE)
    private Date startDate;
    
    @Column(name = "END_DATE")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name = "STATUS")
    private Short status;
    
    public boolean isActive() {
        return status != null && status == 1;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INFRA_HUMAN_ID")
    private RuiInfraHuman infraHumanId;
}
