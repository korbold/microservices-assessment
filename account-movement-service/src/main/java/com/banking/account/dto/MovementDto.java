package com.banking.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Movement
 */
public class MovementDto {
    
    private Long movimientoId;
    
    @NotNull(message = "Date is required")
    private LocalDateTime fecha;
    
    @NotBlank(message = "Movement type is required")
    @Pattern(regexp = "^(Deposito|Retiro)$", message = "Movement type must be Deposito or Retiro")
    private String tipoMovimiento;
    
    @NotNull(message = "Value is required")
    @DecimalMin(value = "0.01", message = "Value must be greater than 0")
    private BigDecimal valor;
    
    @NotNull(message = "Balance is required")
    private BigDecimal saldo;
    
    @NotNull(message = "Account ID is required")
    private Long cuentaId;

    // Constructors
    public MovementDto() {}

    public MovementDto(LocalDateTime fecha, String tipoMovimiento, BigDecimal valor, 
                       BigDecimal saldo, Long cuentaId) {
        this.fecha = fecha;
        this.tipoMovimiento = tipoMovimiento;
        this.valor = valor;
        this.saldo = saldo;
        this.cuentaId = cuentaId;
    }

    // Getters and Setters
    public Long getMovimientoId() {
        return movimientoId;
    }

    public void setMovimientoId(Long movimientoId) {
        this.movimientoId = movimientoId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId;
    }
}
