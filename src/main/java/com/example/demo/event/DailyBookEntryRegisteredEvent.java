package com.example.demo.event;

import com.example.demo.domain.DailyBook;
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
