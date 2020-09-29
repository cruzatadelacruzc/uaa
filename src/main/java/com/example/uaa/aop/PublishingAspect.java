package com.example.uaa.aop;


import com.example.uaa.service.EntryService;
import com.example.uaa.service.payment.PaymentWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;


@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class PublishingAspect {

    private final EntryService entryService;

    @Pointcut("target(com.example.uaa.service.payment.PaymentService)")
    public void registerEntry() {}


    /**
     * Register a deposit entry and an entry
     *
     * @param result instance {@link PaymentWrapper}
     */
    @AfterReturning(value = "registerEntry()", returning = "result")
    public void registerEntryDeposit(PaymentWrapper result) {
        entryService.createMovement(result);
    }
}
