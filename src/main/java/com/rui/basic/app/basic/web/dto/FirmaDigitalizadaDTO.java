package com.rui.basic.app.basic.web.dto;

import lombok.Data;

@Data
public class FirmaDigitalizadaDTO {
    private Long id;
    private String filename;
    private String route;
    private String imagenUrl;
    private Boolean checked;
}