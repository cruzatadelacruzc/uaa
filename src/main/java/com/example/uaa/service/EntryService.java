package com.example.uaa.service;

import com.example.uaa.config.Constants;
import com.example.uaa.domain.Entry;
import com.example.uaa.domain.User;
import com.example.uaa.event.RegisterEntryEventPublisher;
import com.example.uaa.repository.EntryRepository;
import com.example.uaa.repository.UserRepository;
import com.example.uaa.security.AuthoritiesConstants;
import com.example.uaa.security.SecurityUtils;
import com.example.uaa.service.payment.PaymentWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class EntryService {
    private final RegisterEntryEventPublisher publisher;
    private final EntryRepository entryRepository;
    private final UserRepository userRepository;


    /**
     * Create a deposit entry into {@link Entry} entity. Also it's a publisher of event
     *
     * @param paymentWrapper the object contains data to create a Entry
     * @return a Entry instance
     */
    public Entry createMovement(PaymentWrapper paymentWrapper) {

        String email = paymentWrapper.getEmail();
        String username = paymentWrapper.getUsername();
        User user = userRepository.findOneByEmailOrUsername(email, username).orElseThrow(() -> new NullPointerException(
                String.format("User with email %s or %s not found", email, username)));

        Entry entry = new Entry();
        entry
                .setType(Constants.ENTRY_DEPOSIT)
                .setStatus(paymentWrapper.getStatus())
                .setAmount(paymentWrapper.getAmount())
                .setCurrency(paymentWrapper.getCurrency())
                .setDate(paymentWrapper.getDate())
                .setUser(user);
        entryRepository.save(entry);
        log.debug("Registered data for Entry: {}", entry);
        publisher.publishPaymentEntryEvent(entry);
        return entry;
    }

    /**
     * Get all entries
     *
     * @param pageable the pagination information
     * @return a page of entry
     */
    @Transactional(readOnly = true)
    public Page<Entry> getAllEntries(Pageable pageable) {
        if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN)) {
            log.debug("Request to get all entries");
            return entryRepository.findAll(pageable);
        }
        String username = SecurityUtils.getCurrentUserLogin().get();
        log.debug("Request to get all entries of the User: {}", username);
        return entryRepository.findAllByUserUsername(pageable, username);
    }

    /**
     * Get a Entry by id
     *
     * @param id identifier to get a entry
     * @return a {@link Entry} if it's founded
     */
    @Transactional(readOnly = true)
    public Optional<Entry> getEntryById(Long id) {
        log.debug("Request to get a Entry by ID: {}", id);
        return entryRepository.findById(id);
    }

    /**
     * Get a Entry by username of owner
     *
     * @param username user's identifier of the entry's owner
     * @return a {@link Entry} if it's founded
     */
    @Transactional(readOnly = true)
    public Page<Entry> getEntriesByUsername(Pageable pageable, String username) {
        log.debug("Request to get a Entry by username: {}", username);
        return entryRepository.findEntriesByUserUsername(pageable, username);
    }
}
