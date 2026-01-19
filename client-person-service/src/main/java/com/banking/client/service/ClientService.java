package com.banking.client.service;

import com.banking.client.dto.ClientDto;
import com.banking.client.entity.Client;
import com.banking.client.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Client operations
 */
@Service
@Transactional
public class ClientService {
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private ClientEventPublisher eventPublisher;
    
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);
    
    /**
     * Create a new client
     * @param clientDto the client data
     * @return the created client
     * @throws IllegalArgumentException if client with identification already exists
     */
    public ClientDto createClient(ClientDto clientDto) {
        logger.info("Creating new client with identification: {}", clientDto.getIdentificacion());
        
        if (clientRepository.existsByIdentificacion(clientDto.getIdentificacion())) {
            logger.warn("Attempt to create client with existing identification: {}", clientDto.getIdentificacion());
            throw new IllegalArgumentException("Client with identification " + clientDto.getIdentificacion() + " already exists");
        }
        
        // Encrypt password before saving
        String encryptedPassword = passwordEncoder.encode(clientDto.getContrasena());
        
        Client client = new Client(
            clientDto.getNombre(),
            clientDto.getGenero(),
            clientDto.getEdad(),
            clientDto.getIdentificacion(),
            clientDto.getDireccion(),
            clientDto.getTelefono(),
            encryptedPassword,
            clientDto.getEstado()
        );
        
        Client savedClient = clientRepository.save(client);
        logger.info("Client created successfully with ID: {}, identification: {}", savedClient.getClienteId(), savedClient.getIdentificacion());
        
        // Publish client created event asynchronously via Kafka
        ClientDto savedClientDto = convertToDto(savedClient);
        eventPublisher.publishClientCreated(savedClientDto);
        
        return savedClientDto;
    }
    
    /**
     * Get all clients with pagination
     * @param pageable pagination parameters
     * @return paginated list of clients
     */
    @Transactional(readOnly = true)
    public Page<ClientDto> getAllClients(Pageable pageable) {
        return clientRepository.findAll(pageable)
                .map(this::convertToDto);
    }
    
    /**
     * Get client by ID
     * @param id the client ID
     * @return the client if found
     */
    @Transactional(readOnly = true)
    public Optional<ClientDto> getClientById(Long id) {
        return clientRepository.findById(id)
                .map(this::convertToDto);
    }
    
    /**
     * Get client by identification
     * @param identificacion the identification number
     * @return the client if found
     */
    @Transactional(readOnly = true)
    public Optional<ClientDto> getClientByIdentificacion(String identificacion) {
        return clientRepository.findByIdentificacion(identificacion)
                .map(this::convertToDto);
    }
    
    /**
     * Update client
     * @param id the client ID
     * @param clientDto the updated client data
     * @return the updated client
     * @throws IllegalArgumentException if client not found
     */
    public ClientDto updateClient(Long id, ClientDto clientDto) {
        logger.info("Updating client with ID: {}", id);
        
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Attempt to update non-existent client with ID: {}", id);
                    return new IllegalArgumentException("Client with ID " + id + " not found");
                });
        
        // Check if identification is being changed and if new one already exists
        if (!existingClient.getIdentificacion().equals(clientDto.getIdentificacion()) &&
            clientRepository.existsByIdentificacion(clientDto.getIdentificacion())) {
            logger.warn("Attempt to update client ID {} with existing identification: {}", id, clientDto.getIdentificacion());
            throw new IllegalArgumentException("Client with identification " + clientDto.getIdentificacion() + " already exists");
        }
        
        existingClient.setNombre(clientDto.getNombre());
        existingClient.setGenero(clientDto.getGenero());
        existingClient.setEdad(clientDto.getEdad());
        existingClient.setIdentificacion(clientDto.getIdentificacion());
        existingClient.setDireccion(clientDto.getDireccion());
        existingClient.setTelefono(clientDto.getTelefono());
        
        // Only update password if a new one is provided
        if (clientDto.getContrasena() != null && !clientDto.getContrasena().isEmpty()) {
            logger.info("Password updated for client ID: {}", id);
            String encryptedPassword = passwordEncoder.encode(clientDto.getContrasena());
            existingClient.setContrasena(encryptedPassword);
        }
        
        existingClient.setEstado(clientDto.getEstado());
        
        Client updatedClient = clientRepository.save(existingClient);
        logger.info("Client updated successfully with ID: {}", id);
        
        // Publish client updated event asynchronously via Kafka
        ClientDto updatedClientDto = convertToDto(updatedClient);
        eventPublisher.publishClientUpdated(updatedClientDto);
        
        return updatedClientDto;
    }
    
    /**
     * Delete client
     * @param id the client ID
     * @throws IllegalArgumentException if client not found
     */
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new IllegalArgumentException("Client with ID " + id + " not found");
        }
        clientRepository.deleteById(id);
        
        // Publish client deleted event asynchronously via Kafka
        eventPublisher.publishClientDeleted(id);
        logger.info("Client deleted successfully with ID: {}", id);
    }
    
    /**
     * Get active clients with pagination
     * @param pageable pagination parameters
     * @return paginated list of active clients
     */
    @Transactional(readOnly = true)
    public Page<ClientDto> getActiveClients(Pageable pageable) {
        return clientRepository.findByEstadoTrue(pageable)
                .map(this::convertToDto);
    }
    
    /**
     * Convert Client entity to ClientDto
     * @param client the client entity
     * @return the client DTO
     */
    private ClientDto convertToDto(Client client) {
        ClientDto dto = new ClientDto();
        dto.setClienteId(client.getClienteId());
        dto.setNombre(client.getNombre());
        dto.setGenero(client.getGenero());
        dto.setEdad(client.getEdad());
        dto.setIdentificacion(client.getIdentificacion());
        dto.setDireccion(client.getDireccion());
        dto.setTelefono(client.getTelefono());
        // Do not include password in DTO response for security
        dto.setEstado(client.getEstado());
        return dto;
    }
}
