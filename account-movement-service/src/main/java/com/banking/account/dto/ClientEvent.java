package com.banking.account.dto;

import java.time.LocalDateTime;

/**
 * Event DTO for client-related events consumed from Kafka
 */
public class ClientEvent {
    
    private String eventType; // CREATED, UPDATED, DELETED
    private Long clienteId;
    private String nombre;
    private String identificacion;
    private Boolean estado;
    private LocalDateTime timestamp;
    
    // Default constructor for JSON deserialization
    public ClientEvent() {}
    
    // Getters and Setters
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public Long getClienteId() {
        return clienteId;
    }
    
    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getIdentificacion() {
        return identificacion;
    }
    
    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }
    
    public Boolean getEstado() {
        return estado;
    }
    
    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "ClientEvent{" +
                "eventType='" + eventType + '\'' +
                ", clienteId=" + clienteId +
                ", nombre='" + nombre + '\'' +
                ", identificacion='" + identificacion + '\'' +
                ", estado=" + estado +
                ", timestamp=" + timestamp +
                '}';
    }
}
