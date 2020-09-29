package com.example.uaa.config;

import com.paypal.base.rest.APIContext;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class PaymentConfiguration {

    private final AppProperties appProperties;

    @Bean
    public APIContext apiContext() {
        return new APIContext(
                appProperties.getPayment().getPaypal().getCredential().getClientId(),
                appProperties.getPayment().getPaypal().getCredential().getClientSecret(),
                appProperties.getPayment().getPaypal().getMode()
        );
    }
}
