package com.example.uaa.event;

import com.example.uaa.config.Constants;
import com.example.uaa.domain.Entry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Class responsible to publish all events
 */
@Slf4j
@Component
@AllArgsConstructor
public class RegisterEntryEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Create a new CreatedPaymentEntryEvent event
     *
     * @param entry information to create a new entry
     */
    public void publishCreatedPaymentEntryEvent(Entry entry) {
        log.debug("Request to publish a CreatedPaymentEntryEvent with: {}", entry);
        final RegisterEntryEvent<Entry> registerEntryEvent = new CreatedPaymentEntryEvent(
                entry,
                entry.getType(),
                true
        );
        eventPublisher.publishEvent(registerEntryEvent);
    }

    /**
     * Create a new ApprovedPaymentEntryEvent event
     *
     * @param entry information to create a new entry
     */
    public void publishApprovedPaymentEntryEvent(Entry entry) {
        log.debug("Request to publish a ApprovedPaymentEntryEvent with: {}", entry);
        final RegisterEntryEvent<Entry> registerEntryEvent = new ApprovedPaymentEntryEvent(
                entry,
                entry.getType(),
                true
        );
        eventPublisher.publishEvent(registerEntryEvent);
    }

    /**
     * Create a new CreatedPaymentEntryEvent or ApprovedPaymentEntryEvent
     *
     * @param entry information to create a new entry
     */
    public void publishPaymentEntryEvent(Entry entry) {
        log.debug("Request to publish a PaymentEntryEvent with: {}", entry);
        if (entry.getStatus().equals(Constants.PAYMENT_STATUS_CREATED)) {
            publishCreatedPaymentEntryEvent(entry);
        } else {
            publishApprovedPaymentEntryEvent(entry);
        }

    }

}
