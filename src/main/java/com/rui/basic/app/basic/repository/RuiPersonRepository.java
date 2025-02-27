package com.rui.basic.app.basic.repository;



import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rui.basic.app.basic.domain.entities.RuiCompany;
import com.rui.basic.app.basic.domain.entities.RuiGenerics;
import com.rui.basic.app.basic.domain.entities.RuiIntermediary;
import com.rui.basic.app.basic.domain.entities.RuiPerson;


@Repository
public interface RuiPersonRepository extends JpaRepository<RuiPerson, Long> {
    boolean existsByDocumentNumber(String documentNumber);
    //----
    Optional<RuiPerson> findByDocumentTypeAndDocumentNumber(
        RuiGenerics documentType, 
        String documentNumber
    );
    
    @Query("SELECT p FROM RuiPerson p WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(p.firstSurname) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<RuiPerson> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT i FROM RuiIntermediary i WHERE i.companyId = :companyId")
    Page<RuiIntermediary> findByCompanyId(@Param("companyId") RuiCompany companyId, Pageable pageable);
}
