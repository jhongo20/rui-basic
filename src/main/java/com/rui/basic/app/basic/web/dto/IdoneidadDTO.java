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
public class IdoneidadDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long idPerson;
    private String documentType;
    private String documentNumber;
    private String firstName;
    private String secondName;
    private String firstSurname;
    private String secondSurname;
    private String courseEntity;
    private Date dateCourse;
    private PersonDTO person;
    private IntermediaryDTO intermediary;
    private Boolean supportUpload;
}
