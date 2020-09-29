package com.example.uaa.service.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Class DTO object to wrapper information of the payment
 */
@Data
public class PaymentDetailsDTO {
    @NotNull
    private Double amount;
    private String currency = "USD";
    @NotNull
    private Long userId;
}
