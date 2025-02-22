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
    
    public void createTransaction(String action, String description, String tableName, Long recordId, RuiUser user) {
        RuiTransaction transaction = new RuiTransaction();
        transaction.setTransactionType(action);
        transaction.setDescription(description);
        transaction.setTableName(tableName);
        transaction.setTableId(recordId);
        transaction.setUserId(user);
        transaction.setTransactionDate(new Date());
        
        transactionRepository.save(transaction);
    }

    public void logSystemAction(String action, String description) {
        RuiTransaction transaction = new RuiTransaction();
        transaction.setTransactionType(action);
        transaction.setDescription(description);
        transaction.setTransactionDate(new Date());
        
        transactionRepository.save(transaction);
    }
}