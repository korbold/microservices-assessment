package com.banking.client.service;

import com.banking.client.dto.ClientDto;
import com.banking.client.dto.ClientEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for publishing client events to Kafka asynchronously
 */
@Service
public class ClientEventPublisher {
    
    private static final String CLIENT_EVENTS_TOPIC = "client-events";
    private static final Logger logger = LoggerFactory.getLogger(ClientEventPublisher.class);
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Publish client created event
     * @param clientDto the created client
     */
    public void publishClientCreated(ClientDto clientDto) {
        ClientEvent event = new ClientEvent(
            "CREATED",
            clientDto.getClienteId(),
            clientDto.getNombre(),
            clientDto.getIdentificacion(),
            clientDto.getEstado()
        );
        publishEvent(event, "create");
    }
    
    /**
     * Publish client updated event
     * @param clientDto the updated client
     */
    public void publishClientUpdated(ClientDto clientDto) {
        ClientEvent event = new ClientEvent(
            "UPDATED",
            clientDto.getClienteId(),
            clientDto.getNombre(),
            clientDto.getIdentificacion(),
            clientDto.getEstado()
        );
        publishEvent(event, "update");
    }
    
    /**
     * Publish client deleted event
     * @param clienteId the deleted client ID
     */
    public void publishClientDeleted(Long clienteId) {
        ClientEvent event = new ClientEvent();
        event.setEventType("DELETED");
        event.setClienteId(clienteId);
        publishEvent(event, "delete");
    }
    
    /**
     * Publish event to Kafka topic asynchronously
     * @param event the event to publish
     * @param operation the operation type for logging
     */
    private void publishEvent(ClientEvent event, String operation) {
        String key = event.getClienteId() != null ? event.getClienteId().toString() : "unknown";
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(CLIENT_EVENTS_TOPIC, key, event);
        
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                logger.info("Client {} event published successfully: clientId={}, offset={}", 
                    operation, event.getClienteId(), result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to publish client {} event: clientId={}", 
                    operation, event.getClienteId(), exception);
            }
        });
    }
}
