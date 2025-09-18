package com.banking.account.controller;

import com.banking.account.dto.MovementDto;
import com.banking.account.service.MovementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for MovementController
 */
@WebMvcTest(MovementController.class)
class MovementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovementService movementService;

    @Autowired
    private ObjectMapper objectMapper;

    private MovementDto movementDto;

    @BeforeEach
    void setUp() {
        movementDto = new MovementDto();
        movementDto.setMovimientoId(1L);
        movementDto.setFecha(LocalDateTime.now());
        movementDto.setTipoMovimiento("Deposito");
        movementDto.setValor(new BigDecimal("500.00"));
        movementDto.setSaldo(new BigDecimal("2500.00"));
        movementDto.setCuentaId(1L);
    }

    @Test
    void createMovement_Success() throws Exception {
        // Given
        when(movementService.createMovement(any(MovementDto.class))).thenReturn(movementDto);

        // When & Then
        mockMvc.perform(post("/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movementDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movimientoId").value(1L))
                .andExpect(jsonPath("$.tipoMovimiento").value("Deposito"))
                .andExpect(jsonPath("$.valor").value(500.00));
    }

    @Test
    void createMovement_InsufficientBalance() throws Exception {
        // Given
        when(movementService.createMovement(any(MovementDto.class)))
                .thenThrow(new IllegalArgumentException("Saldo no disponible"));

        // When & Then
        mockMvc.perform(post("/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movementDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Saldo no disponible"));
    }

    @Test
    void createMovement_ValidationError() throws Exception {
        // Given
        MovementDto invalidMovement = new MovementDto();
        invalidMovement.setTipoMovimiento("InvalidType"); // Invalid movement type

        // When & Then
        mockMvc.perform(post("/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidMovement)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllMovements_Success() throws Exception {
        // Given
        when(movementService.getAllMovements()).thenReturn(Arrays.asList(movementDto));

        // When & Then
        mockMvc.perform(get("/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].tipoMovimiento").value("Deposito"));
    }

    @Test
    void getMovementsByAccountId_Success() throws Exception {
        // Given
        when(movementService.getMovementsByAccountId(1L)).thenReturn(Arrays.asList(movementDto));

        // When & Then
        mockMvc.perform(get("/movimientos/cuenta/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].cuentaId").value(1L));
    }

    @Test
    void getMovementsByClienteId_Success() throws Exception {
        // Given
        when(movementService.getMovementsByClienteId(1L)).thenReturn(Arrays.asList(movementDto));

        // When & Then
        mockMvc.perform(get("/movimientos/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].cuentaId").value(1L));
    }

    @Test
    void getMovementsByClienteIdAndDateRange_Success() throws Exception {
        // Given
        LocalDateTime fechaInicio = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime fechaFin = LocalDateTime.of(2024, 12, 31, 23, 59);
        when(movementService.getMovementsByClienteIdAndDateRange(1L, fechaInicio, fechaFin))
                .thenReturn(Arrays.asList(movementDto));

        // When & Then
        mockMvc.perform(get("/movimientos/cliente/1/fechas")
                .param("fechaInicio", "2024-01-01T00:00:00")
                .param("fechaFin", "2024-12-31T23:59:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].cuentaId").value(1L));
    }
}
