package com.banking.account.feign;

import com.banking.account.dto.ClientInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation for ClientFeignClient when the service is unavailable
 */
@Component
public class ClientFeignClientFallback implements ClientFeignClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientFeignClientFallback.class);
    
    @Override
    public ClientInfoDto getClientById(Long clienteId) {
        logger.warn("Client service unavailable. Returning fallback data for client ID: {}", clienteId);
        
        // Return a default ClientInfoDto with minimal information
        ClientInfoDto fallback = new ClientInfoDto();
        fallback.setClienteId(clienteId);
        fallback.setNombre("Cliente no disponible");
        fallback.setEstado(false);
        
        return fallback;
    }
}
