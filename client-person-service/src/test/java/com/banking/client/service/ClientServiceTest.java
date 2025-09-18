package com.banking.client.service;

import com.banking.client.dto.ClientDto;
import com.banking.client.entity.Client;
import com.banking.client.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientService
 */
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private ClientDto clientDto;
    private Client client;

    @BeforeEach
    void setUp() {
        clientDto = new ClientDto();
        clientDto.setNombre("Jose Lema");
        clientDto.setGenero("M");
        clientDto.setEdad(30);
        clientDto.setIdentificacion("1234567890");
        clientDto.setDireccion("Otavalo sn y principal");
        clientDto.setTelefono("0982547856");
        clientDto.setContrasena("1234");
        clientDto.setEstado(true);

        client = new Client();
        client.setClienteId(1L);
        client.setNombre("Jose Lema");
        client.setGenero("M");
        client.setEdad(30);
        client.setIdentificacion("1234567890");
        client.setDireccion("Otavalo sn y principal");
        client.setTelefono("0982547856");
        client.setContrasena("1234");
        client.setEstado(true);
    }

    @Test
    void createClient_Success() {
        // Given
        when(clientRepository.existsByIdentificacion(anyString())).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        // When
        ClientDto result = clientService.createClient(clientDto);

        // Then
        assertNotNull(result);
        assertEquals("Jose Lema", result.getNombre());
        assertEquals("1234567890", result.getIdentificacion());
        verify(clientRepository).existsByIdentificacion("1234567890");
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void createClient_ClientAlreadyExists_ThrowsException() {
        // Given
        when(clientRepository.existsByIdentificacion(anyString())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> clientService.createClient(clientDto));
        
        assertEquals("Client with identification 1234567890 already exists", exception.getMessage());
        verify(clientRepository).existsByIdentificacion("1234567890");
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void getAllClients_Success() {
        // Given
        List<Client> clients = Arrays.asList(client);
        when(clientRepository.findAll()).thenReturn(clients);

        // When
        List<ClientDto> result = clientService.getAllClients();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jose Lema", result.get(0).getNombre());
        verify(clientRepository).findAll();
    }

    @Test
    void getClientById_Success() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        // When
        Optional<ClientDto> result = clientService.getClientById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Jose Lema", result.get().getNombre());
        verify(clientRepository).findById(1L);
    }

    @Test
    void getClientById_NotFound() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<ClientDto> result = clientService.getClientById(1L);

        // Then
        assertFalse(result.isPresent());
        verify(clientRepository).findById(1L);
    }

    @Test
    void updateClient_Success() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        // When
        ClientDto result = clientService.updateClient(1L, clientDto);

        // Then
        assertNotNull(result);
        assertEquals("Jose Lema", result.getNombre());
        verify(clientRepository).findById(1L);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void updateClient_ClientNotFound_ThrowsException() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> clientService.updateClient(1L, clientDto));
        
        assertEquals("Client with ID 1 not found", exception.getMessage());
        verify(clientRepository).findById(1L);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void deleteClient_Success() {
        // Given
        when(clientRepository.existsById(1L)).thenReturn(true);

        // When
        clientService.deleteClient(1L);

        // Then
        verify(clientRepository).existsById(1L);
        verify(clientRepository).deleteById(1L);
    }

    @Test
    void deleteClient_ClientNotFound_ThrowsException() {
        // Given
        when(clientRepository.existsById(1L)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> clientService.deleteClient(1L));
        
        assertEquals("Client with ID 1 not found", exception.getMessage());
        verify(clientRepository).existsById(1L);
        verify(clientRepository, never()).deleteById(any());
    }
}
