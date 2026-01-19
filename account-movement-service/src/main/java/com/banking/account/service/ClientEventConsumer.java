package com.banking.account.service;

import com.banking.account.dto.ClientEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Service for consuming client events from Kafka asynchronously
 */
@Service
public class ClientEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientEventConsumer.class);
    
    /**
     * Consume client events from Kafka topic asynchronously
     * @param event the client event
     * @param key the event key (client ID)
     * @param partition the partition number
     * @param offset the offset
     */
    @KafkaListener(topics = "client-events", groupId = "account-movement-service-group")
    public void consumeClientEvent(
            @Payload ClientEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        logger.info("Received client event asynchronously: eventType={}, clientId={}, partition={}, offset={}", 
            event.getEventType(), event.getClienteId(), partition, offset);
        
        try {
            // Process event asynchronously based on event type
            switch (event.getEventType()) {
                case "CREATED":
                    handleClientCreated(event);
                    break;
                case "UPDATED":
                    handleClientUpdated(event);
                    break;
                case "DELETED":
                    handleClientDeleted(event);
                    break;
                default:
                    logger.warn("Unknown event type: {}", event.getEventType());
            }
            
            logger.info("Successfully processed client event: eventType={}, clientId={}", 
                event.getEventType(), event.getClienteId());
                
        } catch (Exception e) {
            logger.error("Error processing client event: eventType={}, clientId={}", 
                event.getEventType(), event.getClienteId(), e);
            // In a production environment, you might want to send to a dead-letter queue
            throw e; // Re-throw to trigger retry mechanism
        }
    }
    
    /**
     * Handle client created event
     * @param event the client event
     */
    private void handleClientCreated(ClientEvent event) {
        logger.info("Handling client created event asynchronously: clientId={}, nombre={}", 
            event.getClienteId(), event.getNombre());
        // Here you could:
        // - Cache client information locally
        // - Update local client reference data
        // - Trigger other business logic
    }
    
    /**
     * Handle client updated event
     * @param event the client event
     */
    private void handleClientUpdated(ClientEvent event) {
        logger.info("Handling client updated event asynchronously: clientId={}, nombre={}, estado={}", 
            event.getClienteId(), event.getNombre(), event.getEstado());
        // Here you could:
        // - Update cached client information
        // - If client is deactivated, mark related accounts as inactive
        // - Trigger other business logic
    }
    
    /**
     * Handle client deleted event
     * @param event the client event
     */
    private void handleClientDeleted(ClientEvent event) {
        logger.info("Handling client deleted event asynchronously: clientId={}", 
            event.getClienteId());
        // Here you could:
        // - Remove cached client information
        // - Mark related accounts as inactive
        // - Trigger other business logic
    }
}
