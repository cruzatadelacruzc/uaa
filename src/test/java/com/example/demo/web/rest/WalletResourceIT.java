package com.example.demo.web.rest;

import com.example.demo.UaaApplication;
import com.example.demo.domain.User;
import com.example.demo.domain.Wallet;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.service.WalletService;
import com.example.demo.service.dto.WalletDTO;
import com.example.demo.web.rest.error.ExceptionTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link WalletResource} REST controller.
 */
@SpringBootTest(classes = UaaApplication.class)
public class WalletResourceIT {

    private static final Double DEFAULT_AMOUNT = Double.parseDouble("10");
    private static final Double UPDATE_AMOUNT = Double.parseDouble("20");

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private Validator validator;

    @Autowired
    private CacheManager cacheManager;

    private MockMvc restMockMvc;

    private Wallet wallet;

    private User user;

    @BeforeEach
    void setUp() {
        WalletResource resource = new WalletResource(walletService);
        this.restMockMvc = MockMvcBuilders.standaloneSetup(resource)
                .setValidator(validator)
                .setControllerAdvice(exceptionTranslator)
                .setMessageConverters(jacksonMessageConverter)
                .build();
    }

    @BeforeEach
    void initTest() {
        this.wallet = new Wallet();
        this.wallet.setTotal_amount(DEFAULT_AMOUNT);
        this.user = new User()
                .setEmail("user@mail.com")
                .setLangKey("en")
                .setLastName("lastName")
                .setFirstName("firstName")
                .setUsername("user")
                .setPassword(TestUtil.generateRandomAlphanumericString(60))
                .setActivated(true);
        this.userRepository.saveAndFlush(this.user);
        this.wallet.setUser(this.user);
    }

    @Test
    void checkEntityWalletWasCached() throws Exception{
        // Initialize the database
        walletRepository.saveAndFlush(this.wallet);
        walletRepository.findById(wallet.getId());
        walletRepository.findById(wallet.getId());
        walletRepository.findById(wallet.getId());
        walletRepository.findById(wallet.getId());


    }
    @Test
    @Transactional
    void updateWalletByUserId() throws Exception {
        // Initialize the database
        walletRepository.saveAndFlush(this.wallet);
        int databaseSizeInitial = walletRepository.findAll().size();

        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setId(this.wallet.getId());
        walletDTO.setTotal_amount(UPDATE_AMOUNT);
        walletDTO.setUserId(this.user.getId());

        restMockMvc.perform(put("/api/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(walletDTO)))
                .andExpect(status().isOk());

        // Validate the Wallet in the database
        List<Wallet> wallets = walletRepository.findAll();
        assertThat(wallets).hasSize(databaseSizeInitial);
        Wallet testWallet = wallets.get(databaseSizeInitial - 1);
        assertThat(testWallet.getTotal_amount()).isEqualTo(UPDATE_AMOUNT);
        assertThat(testWallet.getUser()).isEqualTo(this.user);
    }
}
