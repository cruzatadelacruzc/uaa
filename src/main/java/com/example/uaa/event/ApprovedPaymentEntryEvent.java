package com.example.uaa.event;

import com.example.uaa.domain.Entry;
import lombok.Getter;

@Getter
public class ApprovedPaymentEntryEvent extends RegisterEntryEvent<Entry> {

    private boolean success;

    public ApprovedPaymentEntryEvent(Entry object, String type, boolean success) {
        super(object, type);
        this.success = success;
    }

    public ApprovedPaymentEntryEvent setSuccess(boolean success) {
        this.success = success;
        return this;
    }
}
