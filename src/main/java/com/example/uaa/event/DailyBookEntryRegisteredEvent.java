package com.example.uaa.event;

import com.example.uaa.domain.DailyBook;
import lombok.Getter;

@Getter
public class DailyBookEntryRegisteredEvent {
    private DailyBook dailyBook;
    private boolean entryRegistered;

    public DailyBookEntryRegisteredEvent setDailyBook(DailyBook dailyBook) {
        this.dailyBook = dailyBook;
        return this;
    }

    public DailyBookEntryRegisteredEvent setEntryRegistered(boolean entryRegistered) {
        this.entryRegistered = entryRegistered;
        return this;
    }
}
