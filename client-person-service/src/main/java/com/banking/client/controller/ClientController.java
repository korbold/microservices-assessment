package com.banking.client.controller;

import com.banking.client.dto.ClientDto;
import com.banking.client.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Client operations
 */
@RestController
@RequestMapping("/clientes")
@CrossOrigin(origins = "*")
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
     * Get all clients
     * @return list of all clients
     */
    @GetMapping
    public ResponseEntity<List<ClientDto>> getAllClients() {
        List<ClientDto> clients = clientService.getAllClients();
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
     * Get active clients
     * @return list of active clients
     */
    @GetMapping("/activos")
    public ResponseEntity<List<ClientDto>> getActiveClients() {
        List<ClientDto> clients = clientService.getActiveClients();
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
