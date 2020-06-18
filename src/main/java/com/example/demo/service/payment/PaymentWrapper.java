package com.example.demo.service.payment;

import lombok.Data;

import java.time.ZonedDateTime;

/**
 * Class wrapper for any type payment
 */
@Data
public class PaymentWrapper {

    private ZonedDateTime date;

    private String amount;

    private String currency;

    private String status;

    private String username;

    private String email;

    private Object sourcePayment;
}
