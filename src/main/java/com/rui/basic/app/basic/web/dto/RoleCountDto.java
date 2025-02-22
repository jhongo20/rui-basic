package com.rui.basic.app.basic.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleCountDto {
    private String roleName;
    private Long userCount;
}
