package com.banking.account.service;

import com.banking.account.dto.MovementDto;
import com.banking.account.dto.ReportDto;
import com.banking.account.entity.Account;
import com.banking.account.entity.Movement;
import com.banking.account.feign.ClientFeignClient;
import com.banking.account.repository.AccountRepository;
import com.banking.account.repository.MovementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Movement operations
 */
@Service
@Transactional
public class MovementService {
    
    @Autowired
    private MovementRepository movementRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private ClientFeignClient clientFeignClient;
    
    /**
     * Create a new movement
     * @param movementDto the movement data
     * @return the created movement
     * @throws IllegalArgumentException if account not found or insufficient balance
     */
    public MovementDto createMovement(MovementDto movementDto) {
        Account account = accountRepository.findById(movementDto.getCuentaId())
                .orElseThrow(() -> new IllegalArgumentException("Account with ID " + movementDto.getCuentaId() + " not found"));
        
        if (!account.getEstado()) {
            throw new IllegalArgumentException("Account is inactive");
        }
        
        // Calculate new balance
        BigDecimal currentBalance = getCurrentBalance(account.getCuentaId());
        BigDecimal newBalance;
        
        if ("Deposito".equals(movementDto.getTipoMovimiento())) {
            newBalance = currentBalance.add(movementDto.getValor());
        } else if ("Retiro".equals(movementDto.getTipoMovimiento())) {
            newBalance = currentBalance.subtract(movementDto.getValor());
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Saldo no disponible");
            }
        } else {
            throw new IllegalArgumentException("Invalid movement type");
        }
        
        Movement movement = new Movement(
            LocalDateTime.now(),
            movementDto.getTipoMovimiento(),
            movementDto.getValor(),
            newBalance,
            movementDto.getCuentaId()
        );
        
        Movement savedMovement = movementRepository.save(movement);
        return convertToDto(savedMovement);
    }
    
    /**
     * Get all movements
     * @return list of all movements
     */
    @Transactional(readOnly = true)
    public List<MovementDto> getAllMovements() {
        return movementRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get movements by account ID
     * @param cuentaId the account ID
     * @return list of movements for the account
     */
    @Transactional(readOnly = true)
    public List<MovementDto> getMovementsByAccountId(Long cuentaId) {
        return movementRepository.findByCuentaIdOrderByFechaDesc(cuentaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get movements by client ID
     * @param clienteId the client ID
     * @return list of movements for the client's accounts
     */
    @Transactional(readOnly = true)
    public List<MovementDto> getMovementsByClienteId(Long clienteId) {
        return movementRepository.findByClienteId(clienteId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get movements by client ID and date range
     * @param clienteId the client ID
     * @param fechaInicio start date
     * @param fechaFin end date
     * @return list of movements in the date range
     */
    @Transactional(readOnly = true)
    public List<MovementDto> getMovementsByClienteIdAndDateRange(Long clienteId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return movementRepository.findByClienteIdAndFechaBetween(clienteId, fechaInicio, fechaFin).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Generate account statement report
     * @param clienteId the client ID
     * @param fechaInicio start date
     * @param fechaFin end date
     * @return list of report entries
     */
    @Transactional(readOnly = true)
    public List<ReportDto> generateAccountStatementReport(Long clienteId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<Movement> movements = movementRepository.findByClienteIdAndFechaBetween(clienteId, fechaInicio, fechaFin);
        
        return movements.stream().map(movement -> {
            try {
                // Get client information from external service
                var clientInfo = clientFeignClient.getClientById(clienteId);
                
                // Get account information
                Account account = accountRepository.findById(movement.getCuentaId()).orElse(null);
                
                return new ReportDto(
                    movement.getFecha(),
                    clientInfo.getNombre(),
                    account != null ? account.getNumeroCuenta() : "N/A",
                    account != null ? account.getTipoCuenta() : "N/A",
                    account != null ? account.getSaldoInicial() : BigDecimal.ZERO,
                    account != null ? account.getEstado() : false,
                    movement.getValor(),
                    movement.getSaldo()
                );
            } catch (Exception e) {
                // Fallback if client service is unavailable
                Account account = accountRepository.findById(movement.getCuentaId()).orElse(null);
                return new ReportDto(
                    movement.getFecha(),
                    "Cliente no disponible",
                    account != null ? account.getNumeroCuenta() : "N/A",
                    account != null ? account.getTipoCuenta() : "N/A",
                    account != null ? account.getSaldoInicial() : BigDecimal.ZERO,
                    account != null ? account.getEstado() : false,
                    movement.getValor(),
                    movement.getSaldo()
                );
            }
        }).collect(Collectors.toList());
    }
    
    /**
     * Get current balance for an account
     * @param cuentaId the account ID
     * @return current balance
     */
    @Transactional(readOnly = true)
    public BigDecimal getCurrentBalance(Long cuentaId) {
        List<Movement> movements = movementRepository.findByCuentaIdOrderByFechaDesc(cuentaId);
        if (movements.isEmpty()) {
            Account account = accountRepository.findById(cuentaId).orElse(null);
            return account != null ? account.getSaldoInicial() : BigDecimal.ZERO;
        }
        return movements.get(0).getSaldo();
    }
    
    /**
     * Convert Movement entity to MovementDto
     * @param movement the movement entity
     * @return the movement DTO
     */
    private MovementDto convertToDto(Movement movement) {
        MovementDto dto = new MovementDto();
        dto.setMovimientoId(movement.getMovimientoId());
        dto.setFecha(movement.getFecha());
        dto.setTipoMovimiento(movement.getTipoMovimiento());
        dto.setValor(movement.getValor());
        dto.setSaldo(movement.getSaldo());
        dto.setCuentaId(movement.getCuentaId());
        return dto;
    }
}
