package com.banking.client.controller;

import com.banking.client.dto.ClientDto;
import com.banking.client.service.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @Test
    void createClient_Success() throws Exception {
        // Given
        when(clientService.createClient(any(ClientDto.class))).thenReturn(clientDto);

        // When & Then
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientDto)))
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
        when(clientService.getAllClients()).thenReturn(Arrays.asList(clientDto));

        // When & Then
        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombre").value("Jose Lema"));
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

        // When & Then
        mockMvc.perform(put("/clientes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientDto)))
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
