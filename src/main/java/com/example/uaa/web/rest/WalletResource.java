package com.example.uaa.web.rest;

import com.example.uaa.service.WalletService;
import com.example.uaa.service.dto.WalletDTO;
import com.example.uaa.web.rest.error.BadRequestAlertException;
import com.example.uaa.web.rest.util.HeaderUtil;
import com.example.uaa.web.rest.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api")
public class WalletResource {

    private final WalletService service;

    @Value("${application.clientApp.name}")
    private String applicationName;

    private static final String ENTITY_NAME = "Wallet";

    public WalletResource(WalletService service) {
        this.service = service;
    }

    /**
     * {@code GET /wallets/{username}/username} : get wallet of the user by username
     *
     * @param username of the User owner of the wallet
     * @return the {@link ResponseEntity} with status {@code 200 (Ok)} and with the body the wallet,
     * or with status {@code 404 (Not Found)}
     */
    @GetMapping("/wallets/{username}/username")
    public ResponseEntity<WalletDTO> getWalletByUsername(@PathVariable String username) {
        log.debug("REST request to get Wallet of the User by username: {}", username);
        return ResponseUtil.wrapOrNotFound(service.getWalletByUsername(username));
    }

    /**
     * {@code PUT /wallets } update info user amount
     *
     * @param walletDTO information to update wallet
     * @return the {@link ResponseEntity} with status {@code 200 (Ok)} and with body the updated wallet,
     * or status {@code 400 (Bad Request)} if the ID is null.
     * @throws BadRequestAlertException {@code 400 (Bad Request)} if the ID is null.
     */
    @PutMapping("/wallets")
    public ResponseEntity<WalletDTO> updateAmountWalletByUser(@Valid @RequestBody WalletDTO walletDTO) {
        log.debug("REST request to update a existing Amount wallet with: {}", walletDTO);
        if (walletDTO.getId() == null) {
            throw new BadRequestAlertException("The given id must not be null", "walletManagement", "idnull");
        }
        Optional<WalletDTO> walletUpdated = service.updateWalletByUser(walletDTO);
        return ResponseUtil.wrapOrNotFound(walletUpdated,
                HeaderUtil.createEntityUpdateAlert(applicationName, true,
                        ENTITY_NAME, walletDTO.getId().toString()));
    }
}
