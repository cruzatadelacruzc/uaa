package com.example.demo.event;

import lombok.Getter;

/**
 *
 */
@Getter
public class RegisterEntryEvent<T> {
    private T object;
    private String type;

    public RegisterEntryEvent(final T object, final String type) {
        this.object = object;
        this.type = type;
    }

    public RegisterEntryEvent<T> setObject(T object) {
        this.object = object;
        return this;
    }

    public RegisterEntryEvent<T> setType(String type) {
        this.type = type;
        return this;
    }

}
