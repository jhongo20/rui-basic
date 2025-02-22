package com.rui.basic.app.basic.web.dto;


import java.util.Date;
import lombok.Data;

@Data
public class InfraestructuraOperativaDTO {
    private Long id;
    private String phone1;
    private String phone2;
    private String phone3;
    private String phoneFax;
    private String email;
    private String addressServiceOffice;
    private Boolean sslService;
    private Date licenseExpDate;
    private Boolean checked; // Para el estado del checkbox
    
    // Soportes (si necesitas manejar documentos)
    private Boolean camaraComercioUploaded;
    private Boolean softwareCertificationUploaded;
    private Boolean equiposTecnologicosUploaded;
}
