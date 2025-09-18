package com.banking.account.repository;

import com.banking.account.entity.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Movement entity
 */
@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {
    
    /**
     * Find movements by account ID
     * @param cuentaId the account ID
     * @return List of movements for the account
     */
    List<Movement> findByCuentaIdOrderByFechaDesc(Long cuentaId);
    
    /**
     * Find movements by account ID and date range
     * @param cuentaId the account ID
     * @param fechaInicio start date
     * @param fechaFin end date
     * @return List of movements in the date range
     */
    @Query("SELECT m FROM Movement m WHERE m.cuentaId = :cuentaId AND m.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY m.fecha DESC")
    List<Movement> findByCuentaIdAndFechaBetween(@Param("cuentaId") Long cuentaId, 
                                                @Param("fechaInicio") LocalDateTime fechaInicio, 
                                                @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Find movements by client ID through account relationship
     * @param clienteId the client ID
     * @return List of movements for the client's accounts
     */
    @Query("SELECT m FROM Movement m JOIN Account a ON m.cuentaId = a.cuentaId WHERE a.clienteId = :clienteId ORDER BY m.fecha DESC")
    List<Movement> findByClienteId(@Param("clienteId") Long clienteId);
    
    /**
     * Find movements by client ID and date range
     * @param clienteId the client ID
     * @param fechaInicio start date
     * @param fechaFin end date
     * @return List of movements in the date range for the client
     */
    @Query("SELECT m FROM Movement m JOIN Account a ON m.cuentaId = a.cuentaId WHERE a.clienteId = :clienteId AND m.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY m.fecha DESC")
    List<Movement> findByClienteIdAndFechaBetween(@Param("clienteId") Long clienteId, 
                                                 @Param("fechaInicio") LocalDateTime fechaInicio, 
                                                 @Param("fechaFin") LocalDateTime fechaFin);
}
