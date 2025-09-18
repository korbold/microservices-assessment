package com.banking.account.service;

import com.banking.account.dto.AccountDto;
import com.banking.account.entity.Account;
import com.banking.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Account operations
 */
@Service
@Transactional
public class AccountService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    /**
     * Create a new account
     * @param accountDto the account data
     * @return the created account
     * @throws IllegalArgumentException if account with number already exists
     */
    public AccountDto createAccount(AccountDto accountDto) {
        if (accountRepository.existsByNumeroCuenta(accountDto.getNumeroCuenta())) {
            throw new IllegalArgumentException("Account with number " + accountDto.getNumeroCuenta() + " already exists");
        }
        
        Account account = new Account(
            accountDto.getNumeroCuenta(),
            accountDto.getTipoCuenta(),
            accountDto.getSaldoInicial(),
            accountDto.getEstado(),
            accountDto.getClienteId()
        );
        
        Account savedAccount = accountRepository.save(account);
        return convertToDto(savedAccount);
    }
    
    /**
     * Get all accounts
     * @return list of all accounts
     */
    @Transactional(readOnly = true)
    public List<AccountDto> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get account by ID
     * @param id the account ID
     * @return the account if found
     */
    @Transactional(readOnly = true)
    public Optional<AccountDto> getAccountById(Long id) {
        return accountRepository.findById(id)
                .map(this::convertToDto);
    }
    
    /**
     * Get account by account number
     * @param numeroCuenta the account number
     * @return the account if found
     */
    @Transactional(readOnly = true)
    public Optional<AccountDto> getAccountByNumeroCuenta(String numeroCuenta) {
        return accountRepository.findByNumeroCuenta(numeroCuenta)
                .map(this::convertToDto);
    }
    
    /**
     * Get accounts by client ID
     * @param clienteId the client ID
     * @return list of accounts for the client
     */
    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsByClienteId(Long clienteId) {
        return accountRepository.findByClienteId(clienteId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Update account
     * @param id the account ID
     * @param accountDto the updated account data
     * @return the updated account
     * @throws IllegalArgumentException if account not found
     */
    public AccountDto updateAccount(Long id, AccountDto accountDto) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account with ID " + id + " not found"));
        
        // Check if account number is being changed and if new one already exists
        if (!existingAccount.getNumeroCuenta().equals(accountDto.getNumeroCuenta()) &&
            accountRepository.existsByNumeroCuenta(accountDto.getNumeroCuenta())) {
            throw new IllegalArgumentException("Account with number " + accountDto.getNumeroCuenta() + " already exists");
        }
        
        existingAccount.setNumeroCuenta(accountDto.getNumeroCuenta());
        existingAccount.setTipoCuenta(accountDto.getTipoCuenta());
        existingAccount.setSaldoInicial(accountDto.getSaldoInicial());
        existingAccount.setEstado(accountDto.getEstado());
        existingAccount.setClienteId(accountDto.getClienteId());
        
        Account updatedAccount = accountRepository.save(existingAccount);
        return convertToDto(updatedAccount);
    }
    
    /**
     * Delete account
     * @param id the account ID
     * @throws IllegalArgumentException if account not found
     */
    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new IllegalArgumentException("Account with ID " + id + " not found");
        }
        accountRepository.deleteById(id);
    }
    
    /**
     * Convert Account entity to AccountDto
     * @param account the account entity
     * @return the account DTO
     */
    private AccountDto convertToDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setCuentaId(account.getCuentaId());
        dto.setNumeroCuenta(account.getNumeroCuenta());
        dto.setTipoCuenta(account.getTipoCuenta());
        dto.setSaldoInicial(account.getSaldoInicial());
        dto.setEstado(account.getEstado());
        dto.setClienteId(account.getClienteId());
        return dto;
    }
}
