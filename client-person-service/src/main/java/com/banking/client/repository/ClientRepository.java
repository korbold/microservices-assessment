package com.banking.client.repository;

import com.banking.client.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Client entity
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    /**
     * Find client by identification
     * @param identificacion the identification number
     * @return Optional containing the client if found
     */
    Optional<Client> findByIdentificacion(String identificacion);
    
    /**
     * Find active clients
     * @return List of active clients
     */
    List<Client> findByEstadoTrue();
    
    /**
     * Find clients by name containing the given text
     * @param nombre the name to search for
     * @return List of clients matching the name
     */
    @Query("SELECT c FROM Client c WHERE c.nombre LIKE %:nombre%")
    List<Client> findByNombreContaining(@Param("nombre") String nombre);
    
    /**
     * Check if client exists by identification
     * @param identificacion the identification number
     * @return true if client exists, false otherwise
     */
    boolean existsByIdentificacion(String identificacion);
}
