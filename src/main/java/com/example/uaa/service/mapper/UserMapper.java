package com.example.uaa.service.mapper;

import com.example.uaa.config.Constants;
import com.example.uaa.domain.Authority;
import com.example.uaa.domain.User;
import com.example.uaa.service.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for the entity {@link User} and its DTO called {@link UserDTO}.
 *
 * Normal mappers are generated using MapStruct, this one is hand-coded as MapStruct
 * support is still in beta, and requires a manual step with an IDE.
 */
@Service
public class UserMapper {

    public List<UserDTO> usersToUserDTOs(List<User> users) {
        return users.stream()
                .filter(Objects::nonNull)
                .map(this::userToUserDTO)
                .collect(Collectors.toList());
    }

    public UserDTO userToUserDTO(User user) {
        return new UserDTO(user);
    }

    public List<User> UserDTOsToUsers(List<UserDTO> UserDTOs) {
        return UserDTOs.stream()
                .filter(Objects::nonNull)
                .map(this::UserDTOToUser)
                .collect(Collectors.toList());
    }

    public User UserDTOToUser(UserDTO UserDTO) {
        if (UserDTO == null) {
            return null;
        } else {
            User user = new User();
            user.setId(UserDTO.getId());
            user.setUsername(UserDTO.getUsername().toLowerCase());
            user.setFirstName(UserDTO.getFirstName());
            user.setLastName(UserDTO.getLastName());
            user.setEmail(UserDTO.getEmail().toLowerCase());
            if (UserDTO.getLangKey() == null) {
                user.setLangKey(Constants.DEFAULT_LANGUAGE);
            } else {
                user.setLangKey(UserDTO.getLangKey());
            }
            user.setActivated(UserDTO.isActivated());
            return user;
        }
    }

    private Set<Authority> authoritiesFromStrings(Set<String> authoritiesAsString) {
        Set<Authority> authorities = new HashSet<>();

        if(authoritiesAsString != null){
            authorities = authoritiesAsString.stream().map(string -> {
                Authority auth = new Authority();
                auth.setName(string);
                return auth;
            }).collect(Collectors.toSet());
        }

        return authorities;
    }

    public User userFromId(Long id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }

}
