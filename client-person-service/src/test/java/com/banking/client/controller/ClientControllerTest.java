package com.banking.client.controller;

import com.banking.client.dto.ClientDto;
import com.banking.client.service.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ClientController
 */
@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Autowired
    private ObjectMapper objectMapper;

    private ClientDto clientDto;

    @BeforeEach
    void setUp() {
        clientDto = new ClientDto();
        clientDto.setClienteId(1L);
        clientDto.setNombre("Jose Lema");
        clientDto.setGenero("M");
        clientDto.setEdad(30);
        clientDto.setIdentificacion("1234567890");
        clientDto.setDireccion("Otavalo sn y principal");
        clientDto.setTelefono("0982547856");
        clientDto.setContrasena("1234");
        clientDto.setEstado(true);
    }
    
    private ClientDto createClientDtoWithoutId() {
        ClientDto dto = new ClientDto();
        dto.setNombre("Jose Lema");
        dto.setGenero("M");
        dto.setEdad(30);
        dto.setIdentificacion("1234567890");
        dto.setDireccion("Otavalo sn y principal");
        dto.setTelefono("0982547856");
        dto.setContrasena("1234");
        dto.setEstado(true);
        return dto;
    }

    @Test
    void createClient_Success() throws Exception {
        // Given
        ClientDto createDto = createClientDtoWithoutId();
        when(clientService.createClient(any(ClientDto.class))).thenReturn(clientDto);

        // When & Then - Create JSON manually to ensure contrasena is included
        String jsonContent = "{"
            + "\"nombre\":\"Jose Lema\","
            + "\"genero\":\"M\","
            + "\"edad\":30,"
            + "\"identificacion\":\"1234567890\","
            + "\"direccion\":\"Otavalo sn y principal\","
            + "\"telefono\":\"0982547856\","
            + "\"contrasena\":\"1234\","
            + "\"estado\":true"
            + "}";
        
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value(1L))
                .andExpect(jsonPath("$.nombre").value("Jose Lema"))
                .andExpect(jsonPath("$.identificacion").value("1234567890"));
    }

    @Test
    void createClient_ValidationError() throws Exception {
        // Given
        ClientDto invalidClient = new ClientDto();
        invalidClient.setNombre(""); // Invalid: empty name

        // When & Then
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidClient)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllClients_Success() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<ClientDto> clientPage = new PageImpl<>(Arrays.asList(clientDto), pageable, 1);
        when(clientService.getAllClients(any(Pageable.class))).thenReturn(clientPage);

        // When & Then
        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].nombre").value("Jose Lema"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getClientById_Success() throws Exception {
        // Given
        when(clientService.getClientById(1L)).thenReturn(Optional.of(clientDto));

        // When & Then
        mockMvc.perform(get("/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(1L))
                .andExpect(jsonPath("$.nombre").value("Jose Lema"));
    }

    @Test
    void getClientById_NotFound() throws Exception {
        // Given
        when(clientService.getClientById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/clientes/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateClient_Success() throws Exception {
        // Given
        when(clientService.updateClient(anyLong(), any(ClientDto.class))).thenReturn(clientDto);

        // When & Then - Create JSON manually to ensure contrasena is included
        String jsonContent = "{"
            + "\"nombre\":\"Jose Lema\","
            + "\"genero\":\"M\","
            + "\"edad\":30,"
            + "\"identificacion\":\"1234567890\","
            + "\"direccion\":\"Otavalo sn y principal\","
            + "\"telefono\":\"0982547856\","
            + "\"contrasena\":\"1234\","
            + "\"estado\":true"
            + "}";

        mockMvc.perform(put("/clientes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(1L))
                .andExpect(jsonPath("$.nombre").value("Jose Lema"));
    }

    @Test
    void deleteClient_Success() throws Exception {
        // Given
        when(clientService.getClientById(1L)).thenReturn(Optional.of(clientDto));

        // When & Then
        mockMvc.perform(delete("/clientes/1"))
                .andExpect(status().isNoContent());
    }
}
