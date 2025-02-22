package com.rui.basic.app.basic.web.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkExperienceDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String company;
    private String charge;
    private String nameBoss;
    private String phoneBoss;
    private Date startDate;
    private Date endDate;
    private Short status;
    private HumanInfraDTO infraHuman;
}
