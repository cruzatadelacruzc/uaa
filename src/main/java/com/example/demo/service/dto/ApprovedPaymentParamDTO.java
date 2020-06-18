package com.example.demo.service.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Class DTO object to wrapper properties to execute approved payment.
 */
@Data
public class ApprovedPaymentParamDTO {
    @NotNull
    private String paymentId;
    @NotNull
    private String payerId;
}
