package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.math.BigInteger;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "V_RUI_CONFIGURATION")
@Data
@NoArgsConstructor
public class VRuiConfiguration implements Serializable {
    
    @Id
    @Column(name = "ID")
    private BigInteger id;
    
    @Column(name = "NAME")
    private String name;
    
    @Column(name = "VALUE")
    private String value;
    
    @Column(name = "DESCRIPTION")
    private String description;
}
