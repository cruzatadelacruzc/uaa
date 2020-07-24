package com.example.demo.service.dto;


import com.example.demo.domain.Authority;
import com.example.demo.domain.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class DTO representing a {@link User} entity
 */
@Data
@NoArgsConstructor
public class UserDTO implements Serializable {

    private Long id;

    @NotBlank
    @Pattern(regexp = "^[_.@A-Za-z0-9-]*$")
    @Size(min = 1, max = 254)
    private String username;

    @Size(max = 254)
    private String firstName;

    @Size(max = 254)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    private String email;

    private boolean activated = false;

    @Size(min = 2, max = 10)
    private String langKey;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    private Set<String> authorities;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.langKey = user.getLangKey();
        this.activated = user.isActivated();
        this.createdBy = user.getCreatedBy();
        this.lastModifiedBy = user.getLastModifiedBy();
        this.createdDate = user.getCreatedDate();
        this.lastModifiedDate = user.getLastModifiedDate();
        this.authorities = user.getAuthorities()
                .stream()
                .map(Authority::getName)
                .collect(Collectors.toSet());
    }


    public UserDTO setUsername(String username) {
        this.username = username;
        return this;
    }

    public UserDTO setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public UserDTO setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public UserDTO setEmail(String email) {
        this.email = email;
        return this;
    }

    public UserDTO setActivated(boolean activated) {
        this.activated = activated;
        return this;
    }

    public UserDTO setLangKey(String langKey) {
        this.langKey = langKey;
        return this;
    }

    public UserDTO setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
        return this;
    }
}
