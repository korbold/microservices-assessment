package com.banking.account.feign;

import com.banking.account.dto.ClientInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for communicating with Client-Person service
 */
@FeignClient(name = "client-person-service")
public interface ClientFeignClient {
    
    /**
     * Get client information by ID
     * @param clienteId the client ID
     * @return client information
     */
    @GetMapping("/clientes/{clienteId}")
    ClientInfoDto getClientById(@PathVariable("clienteId") Long clienteId);
}
