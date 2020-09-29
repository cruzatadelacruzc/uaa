package com.example.uaa.event;

import com.example.uaa.domain.Wallet;
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
