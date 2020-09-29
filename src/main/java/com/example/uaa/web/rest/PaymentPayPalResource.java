package com.example.uaa.web.rest;

import com.example.uaa.service.dto.ApprovedPaymentParamDTO;
import com.example.uaa.service.dto.PaymentDetailsDTO;
import com.example.uaa.service.payment.PaymentService;
import com.example.uaa.web.rest.util.HeaderUtil;
import com.example.uaa.web.rest.util.ResponseUtil;
import com.example.uaa.web.rest.vm.ExecutedPayment;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;


@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentPayPalResource {

    private final PaymentService payPalService;

    @Value("${spring.application.name}")
    private String applicationName;

    public PaymentPayPalResource(PaymentService payPalService) {
        this.payPalService = payPalService;
    }

    /**
     * {@code POST /create }: Create a payment must approved by user
     *
     * @param paymentDetailsDTO information to create the payment
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the approval link,
     * or with status {@code 404 (Not Found)}
     * @throws PayPalRESTException if occur any throwable during creation process
     */
    @PostMapping("/create")
    public ResponseEntity<String> createPayment(@RequestBody PaymentDetailsDTO paymentDetailsDTO) throws PayPalRESTException {
        log.debug("REST request to create payment with {}", paymentDetailsDTO);
        Payment payment = (Payment) payPalService.createPayment(paymentDetailsDTO).getSourcePayment();
        return ResponseUtil.wrapOrNotFound(getApprovalLink(payment));
    }

    /**
     * {@code POST /execute}: To execute a payment
     * @param dto properties to execute payment
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the ExecutedPayment
     * @throws PayPalRESTException if occur any throwable during creation process
     */
    @PostMapping("/execute")
    public ResponseEntity<ExecutedPayment> completePayment(@RequestBody ApprovedPaymentParamDTO dto) throws PayPalRESTException {
        log.debug("REST request to execute payment with {}", dto);
        Payment executedPayment = (Payment) payPalService.executePayment(dto).getSourcePayment();
        ExecutedPayment respObj = new ExecutedPayment();
        respObj
                .setState(executedPayment.getState())
                .setFirstNamePayer(executedPayment.getPayer().getPayerInfo().getFirstName())
                .setLastNamePayer(executedPayment.getPayer().getPayerInfo().getLastName())
                .setEmailPayer(executedPayment.getPayer().getPayerInfo().getEmail())
                .setAmount(executedPayment.getTransactions().get(0).getAmount().getTotal());
        return ResponseEntity.ok()
                .headers(HeaderUtil.createAlert(
                        applicationName, "payment.executed.response", executedPayment.getId()
                ))
                .body(respObj);
    }


    /**
     * Get approval link , once payment has been approval by user
     *
     * @param approvedPayment the PaymentWrapper executed
     * @return Approval link
     */
    private Optional<String> getApprovalLink(Payment approvedPayment) {
        return approvedPayment.getLinks()
                .stream()
                .filter(link -> link.getRel().equalsIgnoreCase("approval_url"))
                .map(Links::getHref)
                .findFirst();

    }

}
