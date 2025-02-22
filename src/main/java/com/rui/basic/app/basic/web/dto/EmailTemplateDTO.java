package com.rui.basic.app.basic.web.dto;

import java.util.Map;

import lombok.Data;

@Data
public class EmailTemplateDTO {
    private String to;
    private String subject;
    private String template;
    private Map<String, String> templateVariables;
}
