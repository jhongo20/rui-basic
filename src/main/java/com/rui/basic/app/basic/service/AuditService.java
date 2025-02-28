package com.rui.basic.app.basic.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.rui.basic.app.basic.domain.entities.RuiTransaction;
import com.rui.basic.app.basic.domain.entities.RuiUser;
import com.rui.basic.app.basic.repository.RuiTransactionRepository;
import java.util.Date;

@Service
@Transactional
@RequiredArgsConstructor
public class AuditService {
    
    private final RuiTransactionRepository transactionRepository;
    
    public void createTransaction(String action, String eventDescription, String tableName, Long recordId, RuiUser user) {
        RuiTransaction transaction = new RuiTransaction();
        transaction.setEventType(action); // Cambiado de transactionType a eventType
        transaction.setEventDescription(eventDescription); // Cambiado de description a eventDescription
        transaction.setTableName(tableName);
        transaction.setTableId(recordId);
        transaction.setUserId(user);
        transaction.setDatetime(new Date()); // Cambiado de transactionDate a datetime
        
        transactionRepository.save(transaction);
    }

    public void logSystemAction(String action, String eventDescription) {
        RuiTransaction transaction = new RuiTransaction();
        transaction.setEventType(action); // Cambiado de transactionType a eventType
        transaction.setEventDescription(eventDescription); // Cambiado de description a eventDescription
        transaction.setDatetime(new Date()); // Cambiado de transactionDate a datetime
        
        transactionRepository.save(transaction);
    }
}