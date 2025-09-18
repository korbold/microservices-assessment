package com.banking.account.controller;

import com.banking.account.dto.AccountDto;
import com.banking.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Account operations
 */
@RestController
@RequestMapping("/cuentas")
@CrossOrigin(origins = "*")
public class AccountController {
    
    @Autowired
    private AccountService accountService;
    
    /**
     * Create a new account
     * @param accountDto the account data
     * @return the created account
     */
    @PostMapping
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountDto accountDto) {
        try {
            AccountDto createdAccount = accountService.createAccount(accountDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Get all accounts
     * @return list of all accounts
     */
    @GetMapping
    public ResponseEntity<List<AccountDto>> getAllAccounts() {
        List<AccountDto> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }
    
    /**
     * Get account by ID
     * @param id the account ID
     * @return the account if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAccountById(@PathVariable Long id) {
        Optional<AccountDto> account = accountService.getAccountById(id);
        if (account.isPresent()) {
            return ResponseEntity.ok(account.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get account by account number
     * @param numeroCuenta the account number
     * @return the account if found
     */
    @GetMapping("/numero/{numeroCuenta}")
    public ResponseEntity<?> getAccountByNumeroCuenta(@PathVariable String numeroCuenta) {
        Optional<AccountDto> account = accountService.getAccountByNumeroCuenta(numeroCuenta);
        if (account.isPresent()) {
            return ResponseEntity.ok(account.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get accounts by client ID
     * @param clienteId the client ID
     * @return list of accounts for the client
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<AccountDto>> getAccountsByClienteId(@PathVariable Long clienteId) {
        List<AccountDto> accounts = accountService.getAccountsByClienteId(clienteId);
        return ResponseEntity.ok(accounts);
    }
    
    /**
     * Update account
     * @param id the account ID
     * @param accountDto the updated account data
     * @return the updated account
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable Long id, @Valid @RequestBody AccountDto accountDto) {
        try {
            AccountDto updatedAccount = accountService.updateAccount(id, accountDto);
            return ResponseEntity.ok(updatedAccount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Delete account
     * @param id the account ID
     * @return no content if successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long id) {
        try {
            accountService.deleteAccount(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Error response class
     */
    public static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
