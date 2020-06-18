package com.example.demo.service.payment;

import com.example.demo.service.dto.ApprovedPaymentParamDTO;
import com.example.demo.service.dto.PaymentDetailsDTO;
import com.paypal.base.rest.PayPalRESTException;

import java.util.Map;

/**
 * PaymentService interface
 */
public interface PaymentService {

    /**
     * Create a payment request
     *
     * @param paymentOrder Information to create the payment
     * @return payment request
     * @throws PayPalRESTException if occur any throwable during creation process
     */
    PaymentWrapper createPayment(PaymentDetailsDTO paymentOrder) throws PayPalRESTException;

    /**
     * Execute approved payment
     *
     * @param approvedPaymentParamDTO properties to execute payment
     * @return Executed payment
     * @throws PayPalRESTException if occur any throwable during creation process
     */
    PaymentWrapper executePayment(ApprovedPaymentParamDTO approvedPaymentParamDTO) throws PayPalRESTException;

    /**
     * Get all redirect Urls like: success, cancel
     *
     * @return map
     */
    Map<String, String> getRedirectURLs();
}
