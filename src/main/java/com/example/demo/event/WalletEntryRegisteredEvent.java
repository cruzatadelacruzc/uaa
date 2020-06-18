package com.example.demo.event;

import com.example.demo.domain.Wallet;
import lombok.Getter;

@Getter
public class WalletEntryRegisteredEvent {
    private Wallet wallet;
    private boolean entryRegistered;

    public WalletEntryRegisteredEvent setWallet(Wallet wallet) {
        this.wallet = wallet;
        return this;
    }

    public WalletEntryRegisteredEvent setEntryRegistered(boolean entryRegistered) {
        this.entryRegistered = entryRegistered;
        return this;
    }

}
