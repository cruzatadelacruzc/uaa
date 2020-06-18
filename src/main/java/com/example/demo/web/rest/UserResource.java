package com.example.demo.web.rest;

import com.example.demo.config.Constants;
import com.example.demo.domain.User;
import com.example.demo.security.AuthoritiesConstants;
import com.example.demo.service.UserService;
import com.example.demo.service.dto.UserDTO;
import com.example.demo.web.rest.error.BadRequestAlertException;
import com.example.demo.web.rest.error.EmailAlreadyUsedException;
import com.example.demo.web.rest.error.UsernameAlreadyUsedException;
import com.example.demo.web.rest.util.HeaderUtil;
import com.example.demo.web.rest.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api")
public class UserResource {

    private final UserService userService;

    @Value("${application.clientApp.name}")
    private String applicationName;

    private static final String ENTITY_NAME = "User";

    public UserResource(UserService userService) {
        this.userService = userService;
    }

    /**
     * {@code GET /users/:username} get a user with authorities
     *
     * @param username of the user
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with the body the user,
     * or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/users/{username:" + Constants.LOGIN_REGEX + "}/username")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        log.debug("REST request to get User: {}", username);
        return ResponseUtil.wrapOrNotFound(userService.findByUsername(username));
    }

    /**
     * {@code GET /users/:id} get a user
     *
     * @param id of the user
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with the body the user,
     * or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        log.debug("REST request to get User by ID: {}", id);
        return ResponseUtil.wrapOrNotFound(userService.findOne(id));
    }

    /**
     * {@code POST /users}: crate a new user
     *
     * @param userDTO information to create user
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new user,
     * or status {@code 400 (Bad Request)} if the login or email is already in use.
     * @throws URISyntaxException       if the Location URI syntax is incorrect.
     * @throws BadRequestAlertException {@code 400 (Bad Request)} if the login or email is already in use.
     */
    @PostMapping("/users")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO) throws URISyntaxException {
        log.debug("REST request to crate a new User with: {}", userDTO);
        if (userDTO.getId() != null) {
            throw new BadRequestAlertException("A new user cannot already have an ID", "userManagement", "idexists");
        }
        if (userService.findUserByEmail(userDTO.getEmail()).isPresent()) {
            throw new EmailAlreadyUsedException();
        }
        if (userService.findByUsername(userDTO.getUsername().toLowerCase()).isPresent()) {
            throw new UsernameAlreadyUsedException();
        }
        User newUser = userService.createUser(userDTO);
        return ResponseEntity.created(new URI("/api/users/" + newUser.getId().toString()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, newUser.getUsername()))
                .body(newUser);
    }

    /**
     * {@code PUT /users }: update info user
     *
     * @param userDTO information to update user
     * @return the {@link ResponseEntity} with status {@code 200 (Ok)} and with body the updated user,
     * or status {@code 400 (Bad Request)} if the login or email is already in use.
     * @throws BadRequestAlertException {@code 400 (Bad Request)} if the login or email is already in use.
     */
    @PutMapping("/users")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody UserDTO userDTO) {
        log.debug("REST request to update a existing User with: {}", userDTO);
        Optional<User> userEmailStored = userService.findUserByEmail(userDTO.getEmail());
        if (userEmailStored.isPresent() && !userEmailStored.get().getId().equals(userDTO.getId())) {
            throw new EmailAlreadyUsedException();
        }
        Optional<UserDTO> userDTOUsernameStored = userService.findByUsername(userDTO.getUsername().toLowerCase());
        if (userDTOUsernameStored.isPresent() && !userDTOUsernameStored.get().getId().equals(userDTO.getId())) {
            throw new UsernameAlreadyUsedException();
        }
        Optional<UserDTO> userUpdated = userService.updateUser(userDTO);
        return ResponseUtil.wrapOrNotFound(userUpdated,
                HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, userDTO.getUsername()));
    }

    /**
     * {@code DELETE /users/:username/username } delete a user by username
     * @param username identifier of the user to delete
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/users/{username:" + Constants.LOGIN_REGEX + "}/username")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteByUsername(@PathVariable String username) {
        log.debug("REST request to delete a User with username: {}", username);
        userService.deleteByUsername(username);
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, ENTITY_NAME, username))
                .build();
    }

}
