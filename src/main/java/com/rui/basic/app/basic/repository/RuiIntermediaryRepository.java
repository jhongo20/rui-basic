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
    
       // En RuiIntermediaryRepository
       // En RuiIntermediaryRepository
@Query(value = """
       WITH latest_assignments AS (
           SELECT 
               a.INTERMEDIARY_ID,
               MAX(a.ID) as max_id,
               MAX(h.DATETIME) as last_update
           FROM RUI_ASSIGNMENTS a
           INNER JOIN RUI_INTERMEDIARY_HISTORY h 
               ON a.INTERMEDIARY_ID = h.INTERMEDIARY_ID
           GROUP BY a.INTERMEDIARY_ID
       )
       SELECT i.*
       FROM RUI_INTERMEDIARIES i
       LEFT JOIN latest_assignments la ON i.ID = la.INTERMEDIARY_ID
       LEFT JOIN RUI_USERS u ON u.ID = (
           SELECT USER_ID 
           FROM RUI_ASSIGNMENTS 
           WHERE ID = la.max_id
       )
       WHERE i.STATE IN (2, 3, 4, 7)
       AND i.RADICATE_NUMBER IS NOT NULL
       ORDER BY 
           la.last_update DESC NULLS LAST,
           i.RADICATE_NUMBER DESC,
           i.ID DESC
   """, countQuery = """
       SELECT COUNT(i.ID)
       FROM RUI_INTERMEDIARIES i
       WHERE i.STATE IN (2, 3, 4, 7)
       AND i.RADICATE_NUMBER IS NOT NULL
   """, nativeQuery = true)
   Page<RuiIntermediary> findAllWithDetails(Pageable pageable);

       @Query(value = "SELECT id, state FROM RUI_INTERMEDIARIES", nativeQuery = true)
       List<Object[]> findAllStatesDirectly();

       @Query("SELECT i FROM RuiIntermediary i WHERE i.infrastructureHumanId.id = :infraHumanId")
        Optional<RuiIntermediary> findByInfrastructureHumanId(@Param("infraHumanId") Long infraHumanId);
       
}
