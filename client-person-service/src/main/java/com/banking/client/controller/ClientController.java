package com.banking.client.controller;

import com.banking.client.dto.ClientDto;
import com.banking.client.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for Client operations
 */
@RestController
@RequestMapping("/clientes")
public class ClientController {
    
    @Autowired
    private ClientService clientService;
    
    /**
     * Create a new client
     * @param clientDto the client data
     * @return the created client
     */
    @PostMapping
    public ResponseEntity<?> createClient(@Valid @RequestBody ClientDto clientDto) {
        try {
            ClientDto createdClient = clientService.createClient(clientDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Get all clients with pagination
     * @param pageable pagination parameters
     * @return paginated list of clients
     */
    @GetMapping
    public ResponseEntity<Page<ClientDto>> getAllClients(
            @PageableDefault(size = 20) Pageable pageable) {
        // Validate and limit page size
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }
        Page<ClientDto> clients = clientService.getAllClients(pageable);
        return ResponseEntity.ok(clients);
    }
    
    /**
     * Get client by ID
     * @param id the client ID
     * @return the client if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getClientById(@PathVariable Long id) {
        Optional<ClientDto> client = clientService.getClientById(id);
        if (client.isPresent()) {
            return ResponseEntity.ok(client.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get client by identification
     * @param identificacion the identification number
     * @return the client if found
     */
    @GetMapping("/identificacion/{identificacion}")
    public ResponseEntity<?> getClientByIdentificacion(@PathVariable String identificacion) {
        Optional<ClientDto> client = clientService.getClientByIdentificacion(identificacion);
        if (client.isPresent()) {
            return ResponseEntity.ok(client.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Update client
     * @param id the client ID
     * @param clientDto the updated client data
     * @return the updated client
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @Valid @RequestBody ClientDto clientDto) {
        try {
            ClientDto updatedClient = clientService.updateClient(id, clientDto);
            return ResponseEntity.ok(updatedClient);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Delete client
     * @param id the client ID
     * @return no content if successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        try {
            clientService.deleteClient(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Get active clients with pagination
     * @param pageable pagination parameters
     * @return paginated list of active clients
     */
    @GetMapping("/activos")
    public ResponseEntity<Page<ClientDto>> getActiveClients(
            @PageableDefault(size = 20) Pageable pageable) {
        // Validate and limit page size
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }
        Page<ClientDto> clients = clientService.getActiveClients(pageable);
        return ResponseEntity.ok(clients);
    }
    
    /**
     * Error response class
     */
    public static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
