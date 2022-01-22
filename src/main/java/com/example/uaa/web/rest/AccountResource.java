package com.example.uaa.web.rest;

import com.example.uaa.domain.User;
import com.example.uaa.service.MailService;
import com.example.uaa.service.UserService;
import com.example.uaa.web.rest.vm.ManagedUserVM;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api")
public class AccountResource {
    private final UserService service;
    private final MailService mailService;

    public AccountResource(UserService service, MailService mailService) {
        this.service = service;
        this.mailService = mailService;
    }

    /**
     * {@code POST /register} register a new user and send a activation email
     * @param userVM the managed user View Model.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUser(@Valid @RequestBody ManagedUserVM userVM) {
        log.debug("REST request to register a user: {}", userVM);
        User user = service.register(userVM, userVM.getPassword());
        mailService.sendActivationEmail(user);
    }

    /**
     * {@code GET  /activate} : activate the registered user.
     *
     * @param key the activation key.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be activated.
     */
    @GetMapping("/activate")
    public void activateAccount(@RequestParam(value = "key") String key) {
        Optional<User> user = service.activateRegistration(key);
        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this activation key");
        }
    }

    private static class AccountResourceException extends RuntimeException {
        public AccountResourceException(String message) {
            super(message);
        }
    }
}
