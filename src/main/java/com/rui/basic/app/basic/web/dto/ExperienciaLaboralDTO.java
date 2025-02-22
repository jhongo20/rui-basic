package com.rui.basic.app.basic.web.dto;

import java.util.Date;
import lombok.Data;

@Data
public class ExperienciaLaboralDTO {
    private Long id;
    private String company;
    private String charge;
    private String nameBoss;
    private String phoneBoss;
    private Date startDate;
    private Date endDate;
    private Short status;
    private Boolean supportUpload;
    private Boolean checked; // Para el estado del checkbox
}