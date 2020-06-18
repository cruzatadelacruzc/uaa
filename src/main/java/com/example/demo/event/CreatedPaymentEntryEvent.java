package com.example.demo.event;

import com.example.demo.domain.Entry;
import lombok.Getter;

@Getter
public class CreatedPaymentEntryEvent  extends RegisterEntryEvent<Entry>{
    private boolean success;

    public CreatedPaymentEntryEvent(Entry object, String type, boolean success) {
        super(object, type);
        this.success = success;
    }

    public CreatedPaymentEntryEvent setSuccess(boolean success) {
        this.success = success;
        return this;
    }
}
