package com.banking.account.repository;

import com.banking.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Account entity
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    /**
     * Find account by account number
     * @param numeroCuenta the account number
     * @return Optional containing the account if found
     */
    Optional<Account> findByNumeroCuenta(String numeroCuenta);
    
    /**
     * Find accounts by client ID
     * @param clienteId the client ID
     * @return List of accounts for the client
     */
    List<Account> findByClienteId(Long clienteId);
    
    /**
     * Find active accounts by client ID
     * @param clienteId the client ID
     * @return List of active accounts for the client
     */
    List<Account> findByClienteIdAndEstadoTrue(Long clienteId);
    
    /**
     * Check if account exists by account number
     * @param numeroCuenta the account number
     * @return true if account exists, false otherwise
     */
    boolean existsByNumeroCuenta(String numeroCuenta);
    
    /**
     * Find accounts by client ID with account type
     * @param clienteId the client ID
     * @param tipoCuenta the account type
     * @return List of accounts matching the criteria
     */
    @Query("SELECT a FROM Account a WHERE a.clienteId = :clienteId AND a.tipoCuenta = :tipoCuenta")
    List<Account> findByClienteIdAndTipoCuenta(@Param("clienteId") Long clienteId, @Param("tipoCuenta") String tipoCuenta);
}
