package com.rui.basic.app.basic.web.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HumanInfraDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Integer employeesNumber;
    private Integer professionalNumber;
    private Integer technicalNumber;
    private Integer auxiliaryNumber;
    private PersonDTO lawyer;
    private PersonDTO professional;
    private List<WorkExperienceDTO> workExperiences;
}
