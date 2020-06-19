package com.example.demo.service;

import com.example.demo.config.Constants;
import com.example.demo.domain.Authority;
import com.example.demo.domain.User;
import com.example.demo.repository.AuthorityRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.dto.UserDTO;
import com.example.demo.service.mapper.UserMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    /**
     * Get a {@link User}
     *
     * @param id identifier of the user
     * @return {@link UserDTO} instance if is present
     */
    @Transactional(readOnly = true)
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
    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername().toLowerCase());
        user.setLastName(userDTO.getLastName());
        user.setFirstName(userDTO.getFirstName());
        user.setActivated(userDTO.isActivated());
        if (userDTO.getLangKey() == null) {
            user.setLangKey(Constants.DEFAULT_LANGUAGE);
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        user.setEmail(userDTO.getEmail().toLowerCase());
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
                            .setEmail(userDTO.getEmail().toLowerCase())
                            .setAuthorities(
                                    userDTO.getAuthorities().stream()
                                            .map(authorityRepository::findById)
                                            .filter(Optional::isPresent)
                                            .map(Optional::get)
                                            .collect(Collectors.toSet())
                            );
                    log.debug("Changed Information for User {}", user);
                    return user;
                })
                .map(userMapper::userToUserDTO);
    }

    /**
     * Delete a User by username
     *
     * @param username of the user
     */
    public void deleteByUsername(String username){
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
        log.debug("Request to get all users");
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


}
