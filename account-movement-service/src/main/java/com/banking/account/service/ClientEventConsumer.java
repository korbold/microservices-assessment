package com.banking.account.service;

import com.banking.account.dto.ClientEvent;
import com.banking.account.entity.Account;
import com.banking.account.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for consuming client events from Kafka asynchronously
 */
@Service
public class ClientEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientEventConsumer.class);
    
    private final AccountRepository accountRepository;
    
    public ClientEventConsumer(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    /**
     * Consume client events from Kafka topic asynchronously
     * @param event the client event
     * @param key the event key (client ID)
     * @param partition the partition number
     * @param offset the offset
     */
    @KafkaListener(topics = "client-events", groupId = "account-movement-service-group")
    @Transactional
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
            logger.error("Error processing client event: eventType={}, clientId={}, error={}", 
                event.getEventType(), event.getClienteId(), e.getMessage(), e);
            // In a production environment, you might want to send to a dead-letter queue
            // Wrap exception with context for better error tracking
            throw new RuntimeException(
                String.format("Failed to process client event: eventType=%s, clientId=%s", 
                    event.getEventType(), event.getClienteId()), e);
        }
    }
    
    /**
     * Handle client created event
     * @param event the client event
     */
    private void handleClientCreated(ClientEvent event) {
        logger.info("Handling client created event asynchronously: clientId={}, nombre={}", 
            event.getClienteId(), event.getNombre());
        
        // When a client is created, we don't need to do anything with accounts
        // because accounts are created after the client exists.
        // This event is mainly for logging and potential future use cases like caching.
        logger.debug("Client created event processed successfully for clientId={}", event.getClienteId());
    }
    
    /**
     * Handle client updated event
     * @param event the client event
     */
    private void handleClientUpdated(ClientEvent event) {
        logger.info("Handling client updated event asynchronously: clientId={}, nombre={}, estado={}", 
            event.getClienteId(), event.getNombre(), event.getEstado());
        
        // If client is deactivated, mark all related accounts as inactive
        if (Boolean.FALSE.equals(event.getEstado())) {
            List<Account> accounts = accountRepository.findByClienteId(event.getClienteId());
            
            if (!accounts.isEmpty()) {
                int updatedCount = 0;
                for (Account account : accounts) {
                    if (Boolean.TRUE.equals(account.getEstado())) {
                        account.setEstado(false);
                        accountRepository.save(account);
                        updatedCount++;
                        logger.info("Deactivated account: cuentaId={}, numeroCuenta={} due to client deactivation", 
                            account.getCuentaId(), account.getNumeroCuenta());
                    }
                }
                logger.info("Deactivated {} account(s) for clientId={}", updatedCount, event.getClienteId());
            } else {
                logger.debug("No accounts found for clientId={} to deactivate", event.getClienteId());
            }
        } else {
            logger.debug("Client is active, no account status changes needed for clientId={}", event.getClienteId());
        }
    }
    
    /**
     * Handle client deleted event
     * @param event the client event
     */
    private void handleClientDeleted(ClientEvent event) {
        logger.info("Handling client deleted event asynchronously: clientId={}", 
            event.getClienteId());
        
        // When a client is deleted, mark all related accounts as inactive
        // We don't delete accounts physically because they may have historical movements
        List<Account> accounts = accountRepository.findByClienteId(event.getClienteId());
        
        if (!accounts.isEmpty()) {
            int updatedCount = 0;
            for (Account account : accounts) {
                if (Boolean.TRUE.equals(account.getEstado())) {
                    account.setEstado(false);
                    accountRepository.save(account);
                    updatedCount++;
                    logger.info("Deactivated account: cuentaId={}, numeroCuenta={} due to client deletion", 
                        account.getCuentaId(), account.getNumeroCuenta());
                }
            }
            logger.info("Deactivated {} account(s) for deleted clientId={}", updatedCount, event.getClienteId());
        } else {
            logger.debug("No accounts found for deleted clientId={}", event.getClienteId());
        }
    }
}
