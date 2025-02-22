package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RUI_GENERICS")
@Data
@NoArgsConstructor
public class RuiGenerics implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rui_generics_seq")
    @SequenceGenerator(name = "rui_generics_seq", sequenceName = "RUI_GENERICS_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "VALUE")
    private String value;

    @Column(name = "STATUS")
    private Integer status;

    @Column(name = "FATHER_ID")
    private Long fatherId;
}
