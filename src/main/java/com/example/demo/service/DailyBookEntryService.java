package com.example.demo.service;

import com.example.demo.config.Constants;
import com.example.demo.domain.DailyBook;
import com.example.demo.domain.Entry;
import com.example.demo.event.DailyBookEntryRegisteredEvent;
import com.example.demo.event.RegisterEntryEvent;
import com.example.demo.repository.DailyBookRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Business logic related with register entry into {@link DailyBook} entity
 */
@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class DailyBookEntryService {

    private final DailyBookRepository dailyBookRepository;

    /**
     * Registering a deposit payment entry
     *
     * @param entry information about deposit entry.
     */
    public DailyBook registerEntry(Entry entry) {
        DailyBook dailyBook = new DailyBook();
        dailyBook.setDate(ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
        dailyBook.setEntry(entry);
        dailyBook.setUser(entry.getUser());
        dailyBook.setConcept(Constants.CONCEPT_CREATED);
        dailyBookRepository.save(dailyBook);
        log.debug("Created Entry into DailyBook: {}", dailyBook);
        return dailyBook;
    }

    /**
     * Create a deposit entry into {@link DailyBook} entity
     *
     * @param entryEvent Event listened fo create a new entry
     * @return a new {@link DailyBookEntryRegisteredEvent} event of type {@link DailyBook}
     */
    @EventListener
    public DailyBookEntryRegisteredEvent registerDepositEntry(final RegisterEntryEvent<Entry> entryEvent) {
        DailyBook dailyBook_registered = registerEntry(entryEvent.getObject());
        return new DailyBookEntryRegisteredEvent()
                .setDailyBook(dailyBook_registered)
                .setEntryRegistered(true);
    }


}
