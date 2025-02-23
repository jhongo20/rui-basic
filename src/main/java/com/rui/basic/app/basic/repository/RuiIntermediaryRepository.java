package com.rui.basic.app.basic.repository;

import java.math.BigInteger;
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
import com.rui.basic.app.basic.domain.enums.IntermediaryState;

@Repository
public interface RuiIntermediaryRepository extends JpaRepository<RuiIntermediary, Long> {
    
    Optional<RuiIntermediary> findByRadicateNumber(String radicateNumber);
    
    @Query("SELECT i FROM RuiIntermediary i WHERE i.personId.documentType = :documentType " +
           "AND i.personId.documentNumber = :documentNumber")
    Optional<RuiIntermediary> findByPersonDocument(
        @Param("documentType") RuiGenerics documentType,
        @Param("documentNumber") String documentNumber
    );
    
    @Query("SELECT i FROM RuiIntermediary i WHERE i.state = :state")
    List<RuiIntermediary> findAllByState(@Param("state") IntermediaryState state);
    
    @Query("SELECT i FROM RuiIntermediary i WHERE i.companyId.nit = :nit")
    Optional<RuiIntermediary> findByCompanyNit(@Param("nit") String nit);

    /**
     * Encuentra un intermediario con todas sus relaciones cargadas
     */
    @Query("SELECT i FROM RuiIntermediary i " +
           "LEFT JOIN FETCH i.personId " +
           "LEFT JOIN FETCH i.companyId " +
           "LEFT JOIN FETCH i.typeIntermediarieId " +
           "LEFT JOIN FETCH i.infrastructureHumanId " +
           "LEFT JOIN FETCH i.infrastructureOperationalId " +
           "LEFT JOIN FETCH i.ruiIdoniedadList " +
           "WHERE i.id = :id")
    Optional<RuiIntermediary> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM RuiIntermediary r WHERE r.state = :state")
    List<RuiIntermediary> findByState(@Param("state") BigInteger state);

    @Query("SELECT r FROM RuiIntermediary r WHERE r.companyId = :companyId")
    RuiIntermediary findByCompanyId(@Param("companyId") RuiCompany companyId);

    @Query("SELECT r FROM RuiIntermediary r WHERE r.personId = :personId")
    RuiIntermediary findByPersonId(@Param("personId") RuiPerson personId);

    // Método para paginación con ordenamiento
    Page<RuiIntermediary> findAll(Pageable pageable);

    // Método para búsqueda con paginación
    @Query("SELECT i FROM RuiIntermediary i " +
    "LEFT JOIN i.personId p " +
    "LEFT JOIN i.companyId c " +
    "LEFT JOIN i.typeIntermediarieId t " +
    "WHERE LOWER(i.radicateNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
    "OR LOWER(CONCAT(COALESCE(p.firstName, ''), ' ', COALESCE(p.firstSurname, ''))) LIKE LOWER(CONCAT('%', :search, '%')) " +
    "OR LOWER(COALESCE(c.name, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
    "OR LOWER(COALESCE(t.value, '')) LIKE LOWER(CONCAT('%', :search, '%'))")
       Page<RuiIntermediary> findBySearchTerm(@Param("search") String search, Pageable pageable);
    
       @Query("SELECT i FROM RuiIntermediary i " +
       "LEFT JOIN FETCH i.companyId c " +
       "LEFT JOIN FETCH c.departmentId d " +
       "LEFT JOIN FETCH i.personId p " +
       "LEFT JOIN FETCH i.typeIntermediarieId " +
       "LEFT JOIN FETCH p.documentType dt " +
       "LEFT JOIN FETCH i.ruiIntermediaryHistoryList h " +  // Historial
       "LEFT JOIN FETCH h.functionaryId f ")  // Funcionario del historial
       Page<RuiIntermediary> findAllWithRelations(Pageable pageable);

       @Query(value = "SELECT id, state FROM RUI_INTERMEDIARIES", nativeQuery = true)
       List<Object[]> findAllStatesDirectly();
}
