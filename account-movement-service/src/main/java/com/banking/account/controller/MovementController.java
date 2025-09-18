package com.banking.account.controller;

import com.banking.account.dto.MovementDto;
import com.banking.account.dto.ReportDto;
import com.banking.account.service.MovementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for Movement operations
 */
@RestController
@RequestMapping("/movimientos")
@CrossOrigin(origins = "*")
public class MovementController {
    
    @Autowired
    private MovementService movementService;
    
    /**
     * Create a new movement
     * @param movementDto the movement data
     * @return the created movement
     */
    @PostMapping
    public ResponseEntity<?> createMovement(@Valid @RequestBody MovementDto movementDto) {
        try {
            MovementDto createdMovement = movementService.createMovement(movementDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMovement);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Get all movements
     * @return list of all movements
     */
    @GetMapping
    public ResponseEntity<List<MovementDto>> getAllMovements() {
        List<MovementDto> movements = movementService.getAllMovements();
        return ResponseEntity.ok(movements);
    }
    
    /**
     * Get movements by account ID
     * @param cuentaId the account ID
     * @return list of movements for the account
     */
    @GetMapping("/cuenta/{cuentaId}")
    public ResponseEntity<List<MovementDto>> getMovementsByAccountId(@PathVariable Long cuentaId) {
        List<MovementDto> movements = movementService.getMovementsByAccountId(cuentaId);
        return ResponseEntity.ok(movements);
    }
    
    /**
     * Get movements by client ID
     * @param clienteId the client ID
     * @return list of movements for the client's accounts
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<MovementDto>> getMovementsByClienteId(@PathVariable Long clienteId) {
        List<MovementDto> movements = movementService.getMovementsByClienteId(clienteId);
        return ResponseEntity.ok(movements);
    }
    
    /**
     * Get movements by client ID and date range
     * @param clienteId the client ID
     * @param fechaInicio start date
     * @param fechaFin end date
     * @return list of movements in the date range
     */
    @GetMapping("/cliente/{clienteId}/fechas")
    public ResponseEntity<List<MovementDto>> getMovementsByClienteIdAndDateRange(
            @PathVariable Long clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        List<MovementDto> movements = movementService.getMovementsByClienteIdAndDateRange(clienteId, fechaInicio, fechaFin);
        return ResponseEntity.ok(movements);
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
