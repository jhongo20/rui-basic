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
public class OperationalInfraDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String phone1;
    private String phone2;
    private String phone3;
    private String phoneFax;
    private String email;
    private String addressServiceOffice;
    private Boolean sslService;
    private Date licenseExpDate;
}
