package com.banking.client.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Client entity extending Person with additional client-specific information
 */
@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "persona_id")
public class Client extends Person {
    
    @Column(name = "cliente_id")
    private Long clienteId;
    
    @NotBlank(message = "Password is required")
    @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters")
    @Column(name = "contrasena", nullable = false, length = 20)
    private String contrasena;
    
    @NotNull(message = "Status is required")
    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    // Constructors
    public Client() {
        super();
    }

    public Client(String nombre, String genero, Integer edad, String identificacion, 
                  String direccion, String telefono, String contrasena, Boolean estado) {
        super(nombre, genero, edad, identificacion, direccion, telefono);
        this.contrasena = contrasena;
        this.estado = estado;
    }

    // Getters and Setters
    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}
