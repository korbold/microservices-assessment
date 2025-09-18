package com.banking.client.service;

import com.banking.client.dto.ClientDto;
import com.banking.client.entity.Client;
import com.banking.client.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    /**
     * Create a new client
     * @param clientDto the client data
     * @return the created client
     * @throws IllegalArgumentException if client with identification already exists
     */
    public ClientDto createClient(ClientDto clientDto) {
        if (clientRepository.existsByIdentificacion(clientDto.getIdentificacion())) {
            throw new IllegalArgumentException("Client with identification " + clientDto.getIdentificacion() + " already exists");
        }
        
        Client client = new Client(
            clientDto.getNombre(),
            clientDto.getGenero(),
            clientDto.getEdad(),
            clientDto.getIdentificacion(),
            clientDto.getDireccion(),
            clientDto.getTelefono(),
            clientDto.getContrasena(),
            clientDto.getEstado()
        );
        
        Client savedClient = clientRepository.save(client);
        return convertToDto(savedClient);
    }
    
    /**
     * Get all clients
     * @return list of all clients
     */
    @Transactional(readOnly = true)
    public List<ClientDto> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client with ID " + id + " not found"));
        
        // Check if identification is being changed and if new one already exists
        if (!existingClient.getIdentificacion().equals(clientDto.getIdentificacion()) &&
            clientRepository.existsByIdentificacion(clientDto.getIdentificacion())) {
            throw new IllegalArgumentException("Client with identification " + clientDto.getIdentificacion() + " already exists");
        }
        
        existingClient.setNombre(clientDto.getNombre());
        existingClient.setGenero(clientDto.getGenero());
        existingClient.setEdad(clientDto.getEdad());
        existingClient.setIdentificacion(clientDto.getIdentificacion());
        existingClient.setDireccion(clientDto.getDireccion());
        existingClient.setTelefono(clientDto.getTelefono());
        existingClient.setContrasena(clientDto.getContrasena());
        existingClient.setEstado(clientDto.getEstado());
        
        Client updatedClient = clientRepository.save(existingClient);
        return convertToDto(updatedClient);
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
    }
    
    /**
     * Get active clients
     * @return list of active clients
     */
    @Transactional(readOnly = true)
    public List<ClientDto> getActiveClients() {
        return clientRepository.findByEstadoTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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
        dto.setContrasena(client.getContrasena());
        dto.setEstado(client.getEstado());
        return dto;
    }
}
