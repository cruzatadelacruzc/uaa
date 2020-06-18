package com.example.demo.web.rest;

import com.example.demo.UaaApplication;
import com.example.demo.domain.Entry;
import com.example.demo.domain.User;
import com.example.demo.repository.EntryRepository;
import com.example.demo.security.AuthoritiesConstants;
import com.example.demo.service.EntryService;
import com.example.demo.service.UserService;
import com.example.demo.service.dto.UserDTO;
import com.example.demo.web.rest.error.ExceptionTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;

import static com.example.demo.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link EntryResource} REST controller.
 */
@SpringBootTest(classes = UaaApplication.class)
public class EntryResourceIT {

    private static final String DEFAULT_TYPE = "DEPOSIT";

    private static final ZonedDateTime DEFAULT_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);

    private static final String DEFAULT_AMOUNT = "25.5";

    private static final String DEFAULT_CURRENCY = "USD";

    private static final String DEFAULT_STATUS = "created";

    private static final String DEFAULT_USERNAME = "cmcruzata";

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntryRepository repository;

    @Autowired
    private UserService userService;

    @Autowired
    private EntryService entryService;

    private MockMvc restMock;

    private Entry entry;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        EntryResource resource = new EntryResource(entryService);
        this.restMock = MockMvcBuilders.standaloneSetup(resource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setMessageConverters(jacksonMessageConverter)
                .setControllerAdvice(exceptionTranslator)
                .build();
    }

    @BeforeEach
    void initTest() {

        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Cesar");
        userDTO.setLastName("Cruzata");
        userDTO.setLangKey("en");
        userDTO.setActivated(true);
        userDTO.setEmail("cmcruzata@uci.cu");
        userDTO.setUsername(DEFAULT_USERNAME);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        this.user = userService.createUser(userDTO);

        entry = new Entry();
        entry.setType(DEFAULT_TYPE);
        entry.setDate(DEFAULT_DATE);
        entry.setAmount(DEFAULT_AMOUNT);
        entry.setStatus(DEFAULT_STATUS);
        entry.setCurrency(DEFAULT_CURRENCY);
        entry.setUser(this.user);
    }

    @Test
    @Transactional
    void testCreateMovement() {
        repository.saveAndFlush(entry);
        assertThat(entry).isNotNull();
        assertThat(entry.getId()).isNotNull();
        assertThat(entry.getUser()).isEqualTo(this.user);
        assertThat(entry.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(entry.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(entry.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(entry.getAmount()).isEqualTo(DEFAULT_AMOUNT);
    }

    @Test
    @WithMockUser(username = DEFAULT_USERNAME, authorities = {"User"})
    void testGetAllEntries() throws Exception {
        // Initialize the database
        repository.saveAndFlush(entry);

        restMock.perform(get("/api/entries?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)))
                .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT)))
                .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)))
                .andExpect(jsonPath("$.[*].user.username").value(hasItem(DEFAULT_USERNAME)))
                .andExpect(jsonPath("$.[*].date").value(hasItem(sameInstant(DEFAULT_DATE))));

    }

    @Test
    void testGetEntryById() throws Exception {
        // Initialize the database
        repository.saveAndFlush(entry);

        restMock.perform(get("/api/entries/" + entry.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value(DEFAULT_TYPE))
                .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT))
                .andExpect(jsonPath("$.status").value(DEFAULT_STATUS))
                .andExpect(jsonPath("$.user.username").value(DEFAULT_USERNAME))
                .andExpect(jsonPath("$.date").value(sameInstant(DEFAULT_DATE)));

    }

    @Test
    void testGetEntryByUsername() throws Exception {
        // Initialize the database
        repository.saveAndFlush(entry);

        restMock.perform(get("/api/entries/" + entry.getUser().getUsername()+ "/username?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)))
                .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT)))
                .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)))
                .andExpect(jsonPath("$.[*].user.username").value(hasItem(DEFAULT_USERNAME)))
                .andExpect(jsonPath("$.[*].date").value(hasItem(sameInstant(DEFAULT_DATE))));

    }

}
