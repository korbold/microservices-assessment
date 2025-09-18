package com.banking.account.service;

import com.banking.account.dto.MovementDto;
import com.banking.account.entity.Account;
import com.banking.account.entity.Movement;
import com.banking.account.feign.ClientFeignClient;
import com.banking.account.repository.AccountRepository;
import com.banking.account.repository.MovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MovementService
 */
@ExtendWith(MockitoExtension.class)
class MovementServiceTest {

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientFeignClient clientFeignClient;

    @InjectMocks
    private MovementService movementService;

    private MovementDto movementDto;
    private Account account;
    private Movement movement;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setCuentaId(1L);
        account.setNumeroCuenta("478758");
        account.setTipoCuenta("Ahorro");
        account.setSaldoInicial(new BigDecimal("2000.00"));
        account.setEstado(true);
        account.setClienteId(1L);

        movementDto = new MovementDto();
        movementDto.setTipoMovimiento("Deposito");
        movementDto.setValor(new BigDecimal("500.00"));
        movementDto.setCuentaId(1L);

        movement = new Movement();
        movement.setMovimientoId(1L);
        movement.setFecha(LocalDateTime.now());
        movement.setTipoMovimiento("Deposito");
        movement.setValor(new BigDecimal("500.00"));
        movement.setSaldo(new BigDecimal("2500.00"));
        movement.setCuentaId(1L);
    }

    @Test
    void createMovement_Deposito_Success() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(movementRepository.findByCuentaIdOrderByFechaDesc(1L)).thenReturn(Arrays.asList());
        when(movementRepository.save(any(Movement.class))).thenReturn(movement);

        // When
        MovementDto result = movementService.createMovement(movementDto);

        // Then
        assertNotNull(result);
        assertEquals("Deposito", result.getTipoMovimiento());
        assertEquals(new BigDecimal("500.00"), result.getValor());
        assertEquals(new BigDecimal("2500.00"), result.getSaldo());
    }

    @Test
    void createMovement_Retiro_Success() {
        // Given
        movementDto.setTipoMovimiento("Retiro");
        movementDto.setValor(new BigDecimal("200.00"));
        
        // Update movement mock to return Retiro
        movement.setTipoMovimiento("Retiro");
        movement.setValor(new BigDecimal("200.00"));
        movement.setSaldo(new BigDecimal("1800.00"));
        
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(movementRepository.findByCuentaIdOrderByFechaDesc(1L)).thenReturn(Arrays.asList());
        when(movementRepository.save(any(Movement.class))).thenReturn(movement);

        // When
        MovementDto result = movementService.createMovement(movementDto);

        // Then
        assertNotNull(result);
        assertEquals("Retiro", result.getTipoMovimiento());
        assertEquals(new BigDecimal("200.00"), result.getValor());
    }

    @Test
    void createMovement_InsufficientBalance_ThrowsException() {
        // Given
        movementDto.setTipoMovimiento("Retiro");
        movementDto.setValor(new BigDecimal("3000.00")); // More than available balance
        
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(movementRepository.findByCuentaIdOrderByFechaDesc(1L)).thenReturn(Arrays.asList());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> movementService.createMovement(movementDto));
        
        assertEquals("Saldo no disponible", exception.getMessage());
    }

    @Test
    void createMovement_AccountNotFound_ThrowsException() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> movementService.createMovement(movementDto));
        
        assertEquals("Account with ID 1 not found", exception.getMessage());
    }

    @Test
    void createMovement_AccountInactive_ThrowsException() {
        // Given
        account.setEstado(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> movementService.createMovement(movementDto));
        
        assertEquals("Account is inactive", exception.getMessage());
    }

    @Test
    void getCurrentBalance_WithMovements() {
        // Given
        when(movementRepository.findByCuentaIdOrderByFechaDesc(1L)).thenReturn(Arrays.asList(movement));

        // When
        BigDecimal result = movementService.getCurrentBalance(1L);

        // Then
        assertEquals(new BigDecimal("2500.00"), result);
    }

    @Test
    void getCurrentBalance_NoMovements() {
        // Given
        when(movementRepository.findByCuentaIdOrderByFechaDesc(1L)).thenReturn(Arrays.asList());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // When
        BigDecimal result = movementService.getCurrentBalance(1L);

        // Then
        assertEquals(new BigDecimal("2000.00"), result);
    }

    @Test
    void getAllMovements_Success() {
        // Given
        when(movementRepository.findAll()).thenReturn(Arrays.asList(movement));

        // When
        List<MovementDto> result = movementService.getAllMovements();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Deposito", result.get(0).getTipoMovimiento());
    }

    @Test
    void getMovementsByAccountId_Success() {
        // Given
        when(movementRepository.findByCuentaIdOrderByFechaDesc(1L)).thenReturn(Arrays.asList(movement));

        // When
        List<MovementDto> result = movementService.getMovementsByAccountId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCuentaId());
    }
}
