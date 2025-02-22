package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RUI_INFRA_OPERATIONAL")
@Data
@NoArgsConstructor
public class RuiInfraOperational implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_INFRA_OPERATIONAL_SEQ")
    @SequenceGenerator(name = "RUI_INFRA_OPERATIONAL_SEQ", sequenceName = "RUI_INFRA_OPERATIONAL_SEQUENCE", allocationSize = 1)
    private Long id;
    
    @Column(name = "PHONE1")
    private String phone1;
    
    @Column(name = "PHONE2")
    private String phone2;
    
    @Column(name = "PHONE3")
    private String phone3;
    
    @Column(name = "PHONE_FAX")
    private String phoneFax;
    
    @Column(name = "EMAIL")
    private String email;
    
    @Column(name = "ADDRESS_SERVICE_OFFICE")
    private String addressServiceOffice;
    
    @Column(name = "SSL_SERVICE")
    private Boolean sslService;
    
    @Column(name = "LICENSE_EXP_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date licenseExpDate;
    
    @OneToOne(mappedBy = "infrastructureOperationalId")
    private RuiIntermediary intermediary;
}
