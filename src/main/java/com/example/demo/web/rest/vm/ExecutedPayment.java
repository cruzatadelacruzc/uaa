package com.example.demo.web.rest.vm;

import lombok.Data;

/**
 *  Object to return as body in executed payment.
 */
@Data
public class ExecutedPayment {
    private String amount;
    private String state;
    private String emailPayer;
    private String firstNamePayer;
    private String lastNamePayer;

    public ExecutedPayment setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public ExecutedPayment setState(String state) {
        this.state = state;
        return this;
    }

    public ExecutedPayment setFirstNamePayer(String firstNamePayer) {
        this.firstNamePayer = firstNamePayer;
        return this;
    }

    public ExecutedPayment setLastNamePayer(String lastNamePayer) {
        this.lastNamePayer = lastNamePayer;
        return this;
    }

    public ExecutedPayment setEmailPayer(String emailPayer) {
        this.emailPayer = emailPayer;
        return this;
    }
}
