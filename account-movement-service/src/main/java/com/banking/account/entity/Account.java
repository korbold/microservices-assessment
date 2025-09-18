package com.banking.account.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * Account entity representing bank accounts
 */
@Entity
@Table(name = "cuentas")
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cuenta_id")
    private Long cuentaId;
    
    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Account number must be 6 digits")
    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 6)
    private String numeroCuenta;
    
    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "^(Ahorro|Corriente|Ahorros)$", message = "Account type must be Ahorro, Corriente, or Ahorros")
    @Column(name = "tipo_cuenta", nullable = false, length = 20)
    private String tipoCuenta;
    
    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.0", message = "Initial balance must be non-negative")
    @Column(name = "saldo_inicial", nullable = false, precision = 10, scale = 2)
    private BigDecimal saldoInicial;
    
    @NotNull(message = "Status is required")
    @Column(name = "estado", nullable = false)
    private Boolean estado = true;
    
    @NotNull(message = "Client ID is required")
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    // Constructors
    public Account() {}

    public Account(String numeroCuenta, String tipoCuenta, BigDecimal saldoInicial, 
                   Boolean estado, Long clienteId) {
        this.numeroCuenta = numeroCuenta;
        this.tipoCuenta = tipoCuenta;
        this.saldoInicial = saldoInicial;
        this.estado = estado;
        this.clienteId = clienteId;
    }

    // Getters and Setters
    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(String tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(BigDecimal saldoInicial) {
        this.saldoInicial = saldoInicial;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
}
