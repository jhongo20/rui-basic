package com.rui.basic.app.basic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rui.basic.app.basic.domain.entities.RuiTransaction;

@Repository
public interface RuiTransactionRepository extends JpaRepository<RuiTransaction, Long> {
    // Métodos personalizados si son necesarios
}
