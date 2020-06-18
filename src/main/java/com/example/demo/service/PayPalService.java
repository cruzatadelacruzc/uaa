package com.example.demo.service;

import com.example.demo.config.AppProperties;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.dto.ApprovedPaymentParamDTO;
import com.example.demo.service.dto.PaymentDetailsDTO;
import com.example.demo.service.payment.PaymentService;
import com.example.demo.service.payment.PaymentWrapper;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * Payment's implementation using PayPal SDk
 */
@Slf4j
@Service
@AllArgsConstructor
public class PayPalService implements PaymentService {

    private static final String PAYMENT_METHOD = "paypal";
    private static final String INTENT_SALE = "sale";
    private final AppProperties appProperties;
    private final MessageSource messageSource;
    private final APIContext apiContext;
    private final UserRepository userRepository;


    @Override
    public PaymentWrapper createPayment(PaymentDetailsDTO paymentOrder) throws PayPalRESTException {
        log.debug("Request to create payment with details: {}", paymentOrder);
        Amount amount = new Amount();
        amount.setCurrency(paymentOrder.getCurrency());
        amount.setTotal(String.format("%.2f", paymentOrder.getAmount()));

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        Locale locale = Locale.forLanguageTag("en");
        String description = messageSource.getMessage("paypal.transaction.description", null, locale);
        transaction.setDescription(description);
        List<Transaction> transactionList = new ArrayList<>();
        transactionList.add(transaction);

        Payer payer = getPayerInformation(paymentOrder);

        RedirectUrls urls = new RedirectUrls();
        urls.setCancelUrl(getRedirectURLs().get("cancel")).setReturnUrl(getRedirectURLs().get("success"));

        Payment requestPayment = new Payment();
        requestPayment.setTransactions(transactionList);
        requestPayment.setPayer(payer);
        requestPayment.setIntent(INTENT_SALE);
        requestPayment.setRedirectUrls(urls);
        Payment createdPayment = requestPayment.create(apiContext);

        return createPaymentWrapper(createdPayment);
    }

    @Override
    public PaymentWrapper executePayment(ApprovedPaymentParamDTO approvedPaymentParamDTO) throws PayPalRESTException {
        log.debug(
                "Request to execute approved payment with PaymentId: {} and PayerId: {}",
                approvedPaymentParamDTO.getPaymentId(),
                approvedPaymentParamDTO.getPayerId()
        );
        Payment executePayment = new Payment();
        executePayment.setId(approvedPaymentParamDTO.getPaymentId());

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(approvedPaymentParamDTO.getPayerId());
        Payment executedPayment = executePayment.execute(apiContext, paymentExecution);

        return createPaymentWrapper(executedPayment);
    }

    @Override
    public Map<String, String> getRedirectURLs() {
        Map<String, String> url = new HashMap<>();
        url.put("cancel", appProperties.getPayment().getPaypal().getUrlCancel());
        url.put("success", appProperties.getPayment().getPaypal().getUrlSuccess());
        return url;
    }

    /**
     * Payer Info configurer
     *
     * @param details information to create the payment
     * @return a Payer
     */
    private Payer getPayerInformation(PaymentDetailsDTO details) {
        Payer payer = new Payer();
        payer.setPaymentMethod(PAYMENT_METHOD);

        PayerInfo info = userRepository.findById(details.getUserId()).map(user -> {
            PayerInfo payerInfo = new PayerInfo();
            payerInfo
                    .setEmail(user.getEmail())
                    .setFirstName(user.getFirstName())
                    .setLastName(user.getLastName());
            return payerInfo;
        }).orElseThrow(() -> new NullPointerException(
                String.format("User not found with ID %s, don't set PayerInfo for PaymentWrapper", details.getUserId())
        ));
        payer.setPayerInfo(info);
        return payer;
    }

    private PaymentWrapper createPaymentWrapper(Payment payment){
        PaymentWrapper paymentWrapper = new PaymentWrapper();
        paymentWrapper.setSourcePayment(payment);
        paymentWrapper.setAmount(payment.getTransactions().get(0).getAmount().getTotal());
        paymentWrapper.setCurrency(payment.getTransactions().get(0).getAmount().getCurrency());
        paymentWrapper.setDate(ZonedDateTime.parse(payment.getCreateTime()));
        paymentWrapper.setEmail(payment.getPayer().getPayerInfo().getEmail());
        paymentWrapper.setStatus(payment.getState());
        return paymentWrapper;
    }
}
