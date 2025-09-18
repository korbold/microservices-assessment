package com.banking.account.controller;

import com.banking.account.dto.ReportDto;
import com.banking.account.service.MovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for Report operations
 */
@RestController
@RequestMapping("/reportes")
@CrossOrigin(origins = "*")
public class ReportController {
    
    @Autowired
    private MovementService movementService;
    
    /**
     * Generate account statement report
     * @param clienteId the client ID
     * @param fechaInicio start date
     * @param fechaFin end date
     * @return account statement report
     */
    @GetMapping
    public ResponseEntity<List<ReportDto>> generateAccountStatementReport(
            @RequestParam Long clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        
        List<ReportDto> report = movementService.generateAccountStatementReport(clienteId, fechaInicio, fechaFin);
        return ResponseEntity.ok(report);
    }
}
