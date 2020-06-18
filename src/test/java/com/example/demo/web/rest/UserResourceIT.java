package com.example.demo.web.rest;

import com.example.demo.UaaApplication;
import com.example.demo.domain.Authority;
import com.example.demo.domain.User;
import com.example.demo.repository.AuthorityRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthoritiesConstants;
import com.example.demo.service.UserService;
import com.example.demo.service.dto.UserDTO;
import com.example.demo.service.mapper.UserMapper;
import com.example.demo.web.rest.error.ExceptionTranslator;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link UserResource} REST controller.
 */
@SpringBootTest(classes = UaaApplication.class)
public class UserResourceIT {

    private static final String DEFAULT_USERNAME = "userpepe";
    private static final String UPDATE_USERNAME = "userjuan";

    private static final String DEFAULT_FIRST_NAME = "Pepe";
    private static final String UPDATE_FIRST_NAME = "Juan";

    private static final String DEFAULT_LAST_NAME = "Marrero";
    private static final String UPDATE_LAST_NAME = "Perez";

    private static final String DEFAULT_EMAIL = "pepe@mail.com";
    private static final String UPDATE_EMAIL = "juan@mail.com";

    private static final String DEFAULT_LANG_KEY = "es";
    private static final String UPDATE_LANG_KEY = "en";

    private static final String DEFAULT_PASSWORD = "passwordpepe";
    private static final String UPDATE_PASSWORD = "passwordjuan";

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private Validator validator;

    private MockMvc restMockMvc;

    private Authority authority;

    private User user;

    @BeforeEach
    void setUp() {
        UserResource userResource = new UserResource(userService);

        this.restMockMvc = MockMvcBuilders.standaloneSetup(userResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setMessageConverters(jacksonMessageConverter)
                .setControllerAdvice(exceptionTranslator)
                .setValidator(validator)
                .build();
    }

    @BeforeEach
    void initTest() {
        this.user = new User();
        this.user.setEmail(DEFAULT_EMAIL);
        this.user.setActivated(true);
        this.user.setFirstName(DEFAULT_FIRST_NAME);
        this.user.setLastName(DEFAULT_LAST_NAME);
        this.user.setLangKey(DEFAULT_LANG_KEY);
        this.user.setUsername(DEFAULT_USERNAME);
        this.user.setPassword(RandomStringUtils.randomAlphanumeric(60));
        this.authority = new Authority().setName(AuthoritiesConstants.ADMIN);
        authorityRepository.saveAndFlush(this.authority);
        Set<Authority> authorities = Collections.singleton(this.authority);
        this.user.setAuthorities(authorities);

    }

    @Test
    @Transactional
    public void createUser() throws Exception {
        // Initialize the database
        int databaseSizeInitial = userRepository.findAll().size();
        authority.setName(AuthoritiesConstants.USER);
        authorityRepository.saveAndFlush(this.authority);

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(DEFAULT_USERNAME);
        userDTO.setActivated(true);
        userDTO.setFirstName(DEFAULT_FIRST_NAME);
        userDTO.setLastName(DEFAULT_LAST_NAME);
        userDTO.setEmail(DEFAULT_EMAIL);
        userDTO.setLangKey(DEFAULT_LANG_KEY);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restMockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
                .andExpect(status().isCreated());

        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeInitial + 1);
        User testUser = userList.get(userList.size() - 1);
        assertThat(testUser.getUsername()).isEqualTo(DEFAULT_USERNAME);
        assertThat(testUser.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testUser.getLangKey()).isEqualTo(DEFAULT_LANG_KEY);
        assertThat(testUser.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(testUser.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
    }

    @Test
    @Transactional
    public void createUserWithIdNotNull() throws Exception {
        // Initialize the database
        int databaseSizeInitial = userRepository.findAll().size();

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername(DEFAULT_USERNAME);
        userDTO.setActivated(true);
        userDTO.setFirstName(DEFAULT_FIRST_NAME);
        userDTO.setLastName(DEFAULT_LAST_NAME);
        userDTO.setEmail(DEFAULT_EMAIL);
        userDTO.setLangKey(DEFAULT_LANG_KEY);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // An entity with an existing ID cannot be created, so this API call must fail
        restMockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
                .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeInitial);

    }

    @Test
    @Transactional
    public void createUserWithEmailAlreadyUsed() throws Exception {
        userRepository.saveAndFlush(user);
        // Initialize the database
        int databaseSizeInitial = userRepository.findAll().size();


        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(DEFAULT_USERNAME);
        userDTO.setActivated(true);
        userDTO.setFirstName(DEFAULT_FIRST_NAME);
        userDTO.setLastName(DEFAULT_LAST_NAME);
        userDTO.setEmail(UPDATE_EMAIL); // Avoid to fire EmailAlreadyUsedException
        userDTO.setLangKey(DEFAULT_LANG_KEY);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));


        // An entity with an existing Email cannot be created, so this API call must fail
        restMockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
                .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeInitial);

    }

    @Test
    @Transactional
    public void createUserWithUsernameAlreadyUsed() throws Exception {
        userRepository.saveAndFlush(user);
        // Initialize the database
        int databaseSizeInitial = userRepository.findAll().size();


        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(UPDATE_USERNAME); // Avoid to fire UsernameAlreadyUsedException
        userDTO.setActivated(true);
        userDTO.setFirstName(DEFAULT_FIRST_NAME);
        userDTO.setLastName(DEFAULT_LAST_NAME);
        userDTO.setEmail(DEFAULT_EMAIL);
        userDTO.setLangKey(DEFAULT_LANG_KEY);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));


        // An entity with an existing Email cannot be created, so this API call must fail
        restMockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
                .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeInitial);

    }

    @Test
    void getUserById() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        restMockMvc.perform(get("/api/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(DEFAULT_USERNAME))
                .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME))
                .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
                .andExpect(jsonPath("$.activated").value(true))
                .andExpect(jsonPath("$.langKey").value(DEFAULT_LANG_KEY));
    }

    @Test
    void getUserWithNoExistingId() throws Exception {

        restMockMvc.perform(get("/api/users/-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByUsername() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        restMockMvc.perform(get("/api/users/" + user.getUsername() + "/username"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(DEFAULT_USERNAME))
                .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME))
                .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
                .andExpect(jsonPath("$.activated").value(true))
                .andExpect(jsonPath("$.langKey").value(DEFAULT_LANG_KEY));
    }

    @Test
    void getUserWithNoExistingUsername() throws Exception {

        restMockMvc.perform(get("/api/users/unknown/username"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void updateUser() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeInitial = userRepository.findAll().size();
        authority.setName(AuthoritiesConstants.USER);
        authorityRepository.saveAndFlush(this.authority);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(UPDATE_USERNAME);
        userDTO.setActivated(false);
        userDTO.setFirstName(UPDATE_FIRST_NAME);
        userDTO.setLastName(UPDATE_LAST_NAME);
        userDTO.setEmail(UPDATE_EMAIL);
        userDTO.setLangKey(UPDATE_LANG_KEY);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restMockMvc.perform(put("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
                .andExpect(status().isOk());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeInitial);
        User testUser = userList.get(databaseSizeInitial - 1);
        assertThat(testUser.getUsername()).isEqualTo(UPDATE_USERNAME);
        assertThat(testUser.getFirstName()).isEqualTo(UPDATE_FIRST_NAME);
        assertThat(testUser.getLastName()).isEqualTo(UPDATE_LAST_NAME);
        assertThat(testUser.getEmail()).isEqualTo(UPDATE_EMAIL);
        assertThat(testUser.getLangKey()).isEqualTo(UPDATE_LANG_KEY);
    }

    @Test
    @Transactional
    public void updateUserExistingEmail() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        User anotherUser = new User();
        anotherUser.setUsername("leopoldo");
        anotherUser.setPassword(RandomStringUtils.random(60));
        anotherUser.setActivated(true);
        anotherUser.setEmail(UPDATE_EMAIL); //the same email
        anotherUser.setFirstName("java");
        anotherUser.setLastName("bartolo");
        anotherUser.setLangKey("en");
        userRepository.saveAndFlush(anotherUser);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(UPDATE_USERNAME);
        userDTO.setActivated(true);
        userDTO.setFirstName(UPDATE_FIRST_NAME);
        userDTO.setLastName(UPDATE_LAST_NAME);
        userDTO.setEmail(UPDATE_EMAIL);// Avoid to fire EmailAlreadyUsedException
        userDTO.setLangKey(UPDATE_LANG_KEY);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restMockMvc.perform(put("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
                .andExpect(status().isBadRequest());

    }

    @Test
    @Transactional
    public void updateUserExistingUsername() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        User anotherUser = new User();
        anotherUser.setUsername(UPDATE_USERNAME);//the same username
        anotherUser.setPassword(RandomStringUtils.random(60));
        anotherUser.setActivated(true);
        anotherUser.setEmail("leopoldo@mail.com");
        anotherUser.setFirstName("java");
        anotherUser.setLastName("bartolo");
        anotherUser.setLangKey("en");
        userRepository.saveAndFlush(anotherUser);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(UPDATE_USERNAME); // Avoid to fire UsernameAlreadyUsedException
        userDTO.setActivated(true);
        userDTO.setFirstName(UPDATE_FIRST_NAME);
        userDTO.setLastName(UPDATE_LAST_NAME);
        userDTO.setEmail(UPDATE_EMAIL);
        userDTO.setLangKey(UPDATE_LANG_KEY);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restMockMvc.perform(put("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
                .andExpect(status().isBadRequest());

    }

    @Test
    @Transactional
    void deleteByUsername() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeInitial = userRepository.findAll().size();

        // Delete the user
        restMockMvc.perform(delete("/api/users/" + user.getUsername() + "/username")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeInitial - 1);
    }

    @Test
    void testUserDTOtoUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName(DEFAULT_FIRST_NAME)
                .setLastName(DEFAULT_LAST_NAME)
                .setEmail(DEFAULT_EMAIL)
                .setLangKey(DEFAULT_LANG_KEY)
                .setActivated(true)
                .setUsername(DEFAULT_USERNAME)
                .setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        User user = userMapper.UserDTOToUser(userDTO);
        assertThat(user.getUsername()).isEqualTo(DEFAULT_USERNAME);
        assertThat(user.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(user.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
        assertThat(user.getLangKey()).isEqualTo(DEFAULT_LANG_KEY);
        assertThat(user.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(user.isActivated()).isTrue();
        assertThat(user.getCreatedDate()).isNotNull();
        assertThat(user.getCreatedBy()).isNull();
        assertThat(user.getLastModifiedDate()).isNotNull();
        assertThat(user.getLastModifiedBy()).isNull();
        assertThat(user.getAuthorities()).extracting("name").containsExactly(AuthoritiesConstants.USER);
    }

    @Test
    void testUserToUserDTO() {
        User user = new User();
        user.setUsername(DEFAULT_USERNAME)
                .setEmail(DEFAULT_EMAIL)
                .setFirstName(DEFAULT_FIRST_NAME)
                .setLastName(DEFAULT_LAST_NAME)
                .setActivated(true)
                .setLangKey(DEFAULT_LANG_KEY)
                .setAuthorities(Collections.singleton(this.authority));
        user.setLastModifiedBy(DEFAULT_USERNAME);
        user.setCreatedBy(DEFAULT_USERNAME);
        user.setLastModifiedDate(Instant.now());
        user.setCreatedDate(Instant.now());

        UserDTO userDTO = userMapper.userToUserDTO(user);
        assertThat(userDTO.getUsername()).isEqualTo(DEFAULT_USERNAME);
        assertThat(userDTO.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(userDTO.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
        assertThat(userDTO.getLangKey()).isEqualTo(DEFAULT_LANG_KEY);
        assertThat(userDTO.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(userDTO.isActivated()).isTrue();
        assertThat(userDTO.getCreatedDate()).isEqualTo(user.getCreatedDate());
        assertThat(userDTO.getCreatedBy()).isEqualTo(user.getCreatedBy());
        assertThat(userDTO.getLastModifiedDate()).isEqualTo(user.getLastModifiedDate());
        assertThat(userDTO.getLastModifiedBy()).isEqualTo(user.getLastModifiedBy());
        assertThat(userDTO.getAuthorities()).containsExactly(AuthoritiesConstants.ADMIN);
    }


}
