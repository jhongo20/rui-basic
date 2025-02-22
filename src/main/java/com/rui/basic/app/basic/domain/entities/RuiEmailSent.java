package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RUI_EMAILS_SENT")
@Data
@NoArgsConstructor
public class RuiEmailSent implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_EMAILS_SEQ")
    @SequenceGenerator(name = "RUI_EMAILS_SEQ", sequenceName = "RUI_EMAILS_SEQUENCE", allocationSize = 1)
    private Long id;
    
    @Column(name = "EMAIL_TO")
    private String emailTo;
    
    @Column(name = "EMAIL_SUBJECT")
    private String emailSubject;
    
    @Column(name = "EMAIL_BODY")
    @Lob
    private String emailBody;
    
    @Column(name = "SENT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentDate;
    
    @Column(name = "STATUS")
    private Short status;
}