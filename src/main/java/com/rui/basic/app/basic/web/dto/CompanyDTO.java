package com.rui.basic.app.basic.web.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String nit;
    private String name;
    private String address;
    private String email;
    private String phone;
    private CityDTO city;
}
