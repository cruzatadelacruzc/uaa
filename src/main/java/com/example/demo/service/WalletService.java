package com.example.demo.service;

import com.example.demo.config.Constants;
import com.example.demo.domain.Entry;
import com.example.demo.domain.Wallet;
import com.example.demo.event.ApprovedPaymentEntryEvent;
import com.example.demo.event.RegisterEntryEvent;
import com.example.demo.event.WalletEntryRegisteredEvent;
import com.example.demo.repository.WalletRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;

    /**
     * Register an deposit entry in wallet
     *
     * @param entry information to create a new entry in wallet
     * @return An wallet entry
     */
    public Wallet registerEntry(Entry entry) {
        Wallet wallet = new Wallet();
        wallet.setDate(ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
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
}
