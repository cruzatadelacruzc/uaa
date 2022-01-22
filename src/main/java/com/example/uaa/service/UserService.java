package com.example.uaa.service;

import com.example.uaa.domain.Authority;
import com.example.uaa.domain.User;
import com.example.uaa.repository.AuthorityRepository;
import com.example.uaa.repository.UserRepository;
import com.example.uaa.security.AuthoritiesConstants;
import com.example.uaa.security.SecurityUtils;
import com.example.uaa.service.dto.UserDTO;
import com.example.uaa.service.mapper.UserMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class UserService {
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private static final String USER_BY_USERNAME = "userByUsername";
    private static final String USER_BY_EMAIL = "userByEmail";
    private static final String USER_BY_ID = "userByID";


    /**
     * Get a {@link User}
     *
     * @param id identifier of the user
     * @return {@link UserDTO} instance if is present
     */
    @Transactional(readOnly = true)
    @Cacheable(value = USER_BY_ID)
    public Optional<UserDTO> findOne(Long id) {
        log.debug("Request to get a User with ID: {}", id);
        return userRepository.findById(id).map(userMapper::userToUserDTO);
    }

    /**
     * Get a {@link User} with {@link Authority}
     *
     * @param username of the user
     * @return {@link UserDTO} instance if is present
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = USER_BY_USERNAME)
    public Optional<UserDTO> findByUsername(String username) {
        log.debug("Request to get a User with username: {}", username);
        return userRepository.findOneWithAuthoritiesByUsernameIgnoreCase(username).map(userMapper::userToUserDTO);
    }

    /**
     * Create a new User
     *
     * @param userDTO Container of the data {@link UserDTO}
     * @return User
     */
    @Caching(evict = {
            @CacheEvict(value = USER_BY_USERNAME, allEntries = true),
            @CacheEvict(value = USER_BY_EMAIL , allEntries = true),
            @CacheEvict(value = USER_BY_ID, key = "#userDTO.id"),
    })
    public User createUser(UserDTO userDTO) {
        User user = userMapper.UserDTOToUser(userDTO);
        user.setPassword(passwordEncoder.encode(UtilService.generateRandomAlphanumericString()));
        if (userDTO.getAuthorities() != null) {
            Set<Authority> authorities = userDTO.getAuthorities()
                    .stream()
                    .map(authorityRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }
        userRepository.save(user);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    /**
     * Update a existing User
     *
     * @param userDTO Container of the data {@link UserDTO}
     * @return UserDTO if is founded
     */
    @Caching(evict = {
            @CacheEvict(value = USER_BY_USERNAME, allEntries = true),
            @CacheEvict(value = USER_BY_EMAIL , allEntries = true),
            @CacheEvict(value = USER_BY_ID, allEntries = true),
    })
    public Optional<UserDTO> updateUser(UserDTO userDTO) {

        return Optional.of(userRepository.findById(userDTO.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(user -> {
                    user
                            .setUsername(userDTO.getUsername().toLowerCase())
                            .setLangKey(userDTO.getLangKey())
                            .setFirstName(userDTO.getFirstName())
                            .setLastName(userDTO.getLastName())
                            .setActivated(userDTO.isActivated())
                            .setEmail(userDTO.getEmail().toLowerCase());
                    if (!userDTO.getAuthorities().isEmpty()) {
                        Set<Authority> managedAuthorities = user.getAuthorities();
                        managedAuthorities.clear();
                        userDTO.getAuthorities().stream()
                                .map(authorityRepository::findById)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .forEach(managedAuthorities::add);
                    }
                    log.debug("Changed Information for User {}", user);
                    return user;
                })
                .map(userMapper::userToUserDTO);
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    @Caching(evict = {
            @CacheEvict(value = USER_BY_USERNAME, allEntries = true),
            @CacheEvict(value = USER_BY_EMAIL , allEntries = true),
            @CacheEvict(value = USER_BY_ID, allEntries = true),
    })
    public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        SecurityUtils.getCurrentUserLogin()
                .flatMap(userRepository::findUserByUsername)
                .ifPresent(user -> {
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    if (email != null) {
                        user.setEmail(email.toLowerCase());
                    }
                    user.setLangKey(langKey);
                    log.debug("Changed Information for User: {}", user);
                });
    }

    /**
     * Delete a User by username
     *
     * @param username of the user
     */
    @Caching(evict = {
            @CacheEvict(value = USER_BY_USERNAME, allEntries = true),
            @CacheEvict(value = USER_BY_EMAIL , allEntries = true),
            @CacheEvict(value = USER_BY_ID, allEntries = true),
    })
    public void deleteByUsername(String username) {
        userRepository.findUserByUsername(username).ifPresent(user -> {
            userRepository.delete(user);
            log.debug("Deleted User: {}", user);
        });
    }

    /**
     * Find a user by email
     *
     * @param email identifier of the user for log in
     * @return a {@link User} if it's founded
     */
    @Cacheable(cacheNames = USER_BY_EMAIL)
    public Optional<User> findUserByEmail(String email) {
        log.debug("Request to get User by email: {} ", email);
        return userRepository.findUserByEmail(email);
    }

    /**
     * Get all pagination users
     *
     * @param pageable
     * @return
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        log.debug("Request to get all paginated users");
        return userRepository.findAll(pageable).map(userMapper::userToUserDTO);
    }

    /**
     * Get the current user with authorities.
     *
     * @return a {@link User} entity with {@link Authority} entity
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities() {
        log.debug("Request to get current user");
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByUsernameIgnoreCase);
    }

    /**
     * Register a new account
     * @param userDTO  the info to create new account
     * @param password  of the new user
     * @return {@link User} instance
     */
    @Caching(evict = {
            @CacheEvict(value = USER_BY_USERNAME, allEntries = true),
            @CacheEvict(value = USER_BY_EMAIL , allEntries = true)
    })
    public User register(UserDTO userDTO, String password) {
        User user = userMapper.UserDTOToUser(userDTO);
        userRepository.findUserByUsername(user.getUsername()).ifPresent(existingUser -> {
            if ( !removeNonActivatedUser(existingUser)) {
                throw new UsernameAlreadyUsedException();
            }
        });

        userRepository.findUserByEmail(user.getEmail()).ifPresent(existingUser -> {
            if (!removeNonActivatedUser(existingUser)) {
                throw new EmailAlreadyUsedException();
            }
        });
        user.setPassword(passwordEncoder.encode(password));
        // new user is not active
        user.setActivated(true);
        user.setActivationKey(UtilService.generateRandomAlphanumericString());
        Set<Authority> authority = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authority::add);
        user.setAuthorities(authority);
        userRepository.save(user);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    /**
     * Check if user is not activated to remove it
     * @param existingUser {@link User} instance to check
     * @return true if user is not activated and false another case
     */
    private boolean removeNonActivatedUser(User existingUser){
        if (existingUser.isActivated()) {
            return false;
        }
        userRepository.delete(existingUser);
        userRepository.flush();
        return true;
    }

    /**
     * Activate account using activate key
     *
     * @param key Activate key
     * @return User if founded
     */
    public Optional<User> activateRegistration(String key) {
        return userRepository.findUserByActivationKey(key).map(user -> {
            user.setActivationKey(null);
            log.debug("Activated user: {}", user);
            return user;
        });
    }
}
