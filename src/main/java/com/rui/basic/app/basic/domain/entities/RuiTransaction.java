package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "RUI_TRANSACTIONS")
@Data
@NoArgsConstructor
@ToString(exclude = {"userId"})
@EqualsAndHashCode(exclude = {"userId"})
public class RuiTransaction implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_TRANSACTIONS_SEQ")
    @SequenceGenerator(name = "RUI_TRANSACTIONS_SEQ", sequenceName = "RUI_TRANSACTIONS_SEQUENCE", allocationSize = 1)
    private Long id;
    
    @Column(name = "IP_ADDRESS")
    private String ipAddress;
    
    @Column(name = "MAC_ADDRESS")
    private String macAddress;
    
    @Column(name = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datetime; // Cambiado de transactionDate a datetime
    
    @Column(name = "EVENT_TYPE")
    private String eventType;
    
    @Column(name = "EVENT_DESCRIPTION")
    private String eventDescription; // Cambiado de description a eventDescription
    
    @Column(name = "TABLE_NAME")
    private String tableName;
    
    @Column(name = "TABLE_ID")
    private Long tableId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private RuiUser userId;
}
