package com.example.uaa.service;

import com.example.uaa.config.Constants;
import com.example.uaa.domain.Entry;
import com.example.uaa.domain.Wallet;
import com.example.uaa.event.ApprovedPaymentEntryEvent;
import com.example.uaa.event.RegisterEntryEvent;
import com.example.uaa.event.WalletEntryRegisteredEvent;
import com.example.uaa.repository.WalletRepository;
import com.example.uaa.service.dto.WalletDTO;
import com.example.uaa.service.mapper.WalletMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;
    private final String WALLETS_BY_USER_USERNAME = "walletsByUserUsername";

    /**
     * Register an deposit entry in wallet
     *
     * @param entry information to create a new entry in wallet
     * @return An wallet entry
     */
    public Wallet registerEntry(Entry entry) {
        Wallet wallet = new Wallet();
        wallet.setUser(entry.getUser());
        double amount = Double.parseDouble(entry.getAmount());
        Optional<Wallet> old_entry = walletRepository.findByUser(entry.getUser());
        if (old_entry.isPresent()) {
            amount += entry.getType().equals(Constants.ENTRY_DEPOSIT) ? old_entry.get().getTotal_amount() : -old_entry.get().getTotal_amount();
        }
        wallet.setTotal_amount(amount);
        walletRepository.save(wallet);
        log.debug("Created entry into Wallet: {}", wallet);
        return wallet;
    }

    /**
     * Create a deposit entry into {@link Wallet} entity
     *
     * @param entryEvent Event listened fo create a new entry
     * @return a new {@link RegisterEntryEvent} event of type {@link Wallet}
     */
    @EventListener
    public WalletEntryRegisteredEvent registerDepositEntry(ApprovedPaymentEntryEvent entryEvent) {
        Wallet wallet = registerEntry(entryEvent.getObject());
        return new WalletEntryRegisteredEvent().setWallet(wallet).setEntryRegistered(true);
    }

    /**
     * Get a {@link Wallet} entity of the User by username
     *
     * @param username of the user
     * @return {@link WalletDTO} instance if is present
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = WALLETS_BY_USER_USERNAME)
    public Optional<WalletDTO> getWalletByUsername(String username) {
        log.debug("Request to get wallet of the User by username: {}", username);
        return walletRepository.findByUserUsername(username).map(walletMapper::toDto);
    }

    /**
     * Update a existing Wallet
     *
     * @param walletDTO Container of the data {@link WalletDTO}
     * @return WalletDTO if is founded
     */
    @CacheEvict(value = WALLETS_BY_USER_USERNAME, allEntries = true)
    public Optional<WalletDTO> updateWalletByUser(WalletDTO walletDTO) {
        return Optional.of(walletRepository.findByUserId(walletDTO.getUserId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(wallet -> {
                    wallet.setTotal_amount(walletDTO.getTotal_amount());
                    log.debug("Changed Information(Amount) for Wallet {}", wallet);
                    return wallet;
                })
                .map(walletMapper::toDto);
    }
}
