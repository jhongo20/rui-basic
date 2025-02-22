package com.rui.basic.app.basic.web.dto;


import java.util.Date;
import lombok.Data;

@Data
public class IdoneidadProfesionalDTO {
    private Long id;
    private String documentType;
    private String documentNumber;
    private String firstName;
    private String secondName;
    private String firstSurname;
    private String secondSurname;
    private String courseEntity;
    private Date dateCourse;
    private Boolean supportUpload;
    private String documentPath;
    private Boolean checked;  // para manejar el estado del checkbox
}
